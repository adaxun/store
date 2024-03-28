package com.wenxun.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import com.wenxun.constant.MessageConstant;
import com.wenxun.dto.OrderDTO;
import com.wenxun.dto.OrderTokenDTO;
import com.wenxun.entity.*;
import com.wenxun.exception.*;
import com.wenxun.mapper.OrderInfoMapper;
import com.wenxun.mapper.SerialNumberMapper;
import com.wenxun.service.ItemService;
import com.wenxun.service.OrderService;
import com.wenxun.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.concurrent.Future;

/**
 * @author wenxun
 * @date 2024.03.09 12:55
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {


    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private SerialNumberMapper serialNumberMapper;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 每秒产生1000. 个令牌
     */
    private RateLimiter rateLimiter = RateLimiter.create(1000.0);

    /**
     * 生成订单id 日期+index： 20240309+000000000001 total：20位
     * 便于根据日期 分表
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderId(){
        SerialNumber serialNumber = serialNumberMapper.selectByPrimaryKey("order_serial");

        StringBuilder str = new StringBuilder();
        str.append(TimeUtils.format(new Date(),"yyyyMMdd"));

        Integer value = serialNumber.getValue();
        String prefix = "000000000000".substring(value.toString().length());
        str.append(prefix).append(value);

        serialNumber.setValue(value+serialNumber.getStep());
        serialNumberMapper.updateByPrimaryKey(serialNumber);

        return str.toString();
    }

    @Override
    public void prepareOrder(OrderTokenDTO orderDTO) {
        /**
         * 非阻塞的获取令牌，如果获取不到，就抛出异常
         */
        if(!rateLimiter.tryAcquire()){
            throw new RateLimitErrorException(MessageConstant.NET_ERROR);
        }
        /**
         * 验证限流用的令牌
         */
        String tokenKey = "promotion:token:"+orderDTO.getUserId()+":"+orderDTO.getPromotionId()+":"+orderDTO.getItemId();
        String promotionToken =(String) redisTemplate.opsForValue().get(tokenKey);
        if(orderDTO.getPromotionToken()==null||promotionToken==null||!promotionToken.equals(orderDTO.getPromotionToken())){
            throw new PromotionErrorException(MessageConstant.PROMOTION_TOKEN_ERROR);
        }
        OrderDTO newOrderDTO=new OrderDTO();
        BeanUtils.copyProperties(orderDTO,newOrderDTO);
        Future future = taskExecutor.submit(()->{
            this.createOrderAsync(newOrderDTO);
            return null;
        });
        try {
            future.get();
        }catch (Exception e){
            throw new StockNotEnoughException(MessageConstant.STOCK_NOT_ENOUGH);
        }
    }

    /**
     * 对生成订单进行解耦，在大闸的时候 预先校验下单参数
     * @param orderDTO
     */
    @Transactional
    @Override
    public void createOrder(OrderDTO orderDTO) {

        //校验下单参数合法性
        if(orderDTO.getAmount()==null || orderDTO.getAmount()<1||orderDTO.getItemId()==null
        ||orderDTO.getUserId()==null||orderDTO.getPromotionId()==null){
            throw new OrderParamException(MessageConstant.ORDER_PARAM_ERROR);
        }
//        //校验用户
//        UserInfo userInfo = userService.getById(orderDTO.getUserId());
//        if(userInfo==null){
//            throw  new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
//        }
        //校验商品
        Item item = itemService.getById(orderDTO.getItemId());
        if(item==null){
            throw  new ItemNotFoundException(MessageConstant.Item_NOT_FOUND);
        }
//        //校验库存
//        Integer stock = item.getItemStock().getStock();
//        if(stock<orderDTO.getAmount()){
//            throw new StockNotEnoughException(MessageConstant.STOCK_NOT_ENOUGH);
//        }
//
//        //校验活动：是否存在/时间满足条件
//        Promotion promotion = item.getPromotion();
//        if(promotion==null){
//            throw new PromotionErrorException(MessageConstant.PROMOTION_NOT_FOUND);
//        }
//        else if(!promotion.getStatus()){
//            throw  new PromotionErrorException(MessageConstant.PROMOTION_TIME_ERROR);
//        }

//        //undate：扣减库存-> MQ异步更新库存 24.3.14
//        boolean flag = itemService.decreaseStock(orderDTO.getItemId(),orderDTO.getAmount());
//        if(!flag){
//            throw new StockNotEnoughException(MessageConstant.STOCK_NOT_ENOUGH);
//        }

        //生成订单
        OrderInfo orderInfo =new OrderInfo();
        orderInfo.setId(this.generateOrderId());

        BeanUtils.copyProperties(orderDTO,orderInfo);
        orderInfo.setOrderAmount(orderDTO.getAmount());
        orderInfo.setOrderPrice(item.getPromotion().getPromotionPrice());
        orderInfo.setOrderTime(new Timestamp(System.currentTimeMillis()));

        BigDecimal total = orderInfo.getOrderPrice().multiply(new BigDecimal(orderDTO.getAmount()));
        orderInfo.setOrderTotal(total);

        orderInfoMapper.insert(orderInfo);

        /**
         * 更新销量
         * 事务加锁，效率低，可以改为消息队列 异步更新
         */
//        itemService.updateSales(orderDTO.getItemId(),orderDTO.getAmount());
        JSONObject payload = new JSONObject();
        payload.put("itemId", orderDTO.getItemId());
        payload.put("amount", orderDTO.getAmount());
        Message message= MessageBuilder.withPayload(payload).build();
        rocketMQTemplate.asyncSend("order:increaseSales",message,new SendCallback(){
            @Override
            public void onSuccess(SendResult sendResult) {
            log.info("更新销量成功");
            }

            @Override
            public void onException(Throwable throwable) {
            log.error("更新消息失败");
            }
        },60*1000);

    }

    /**
     * 发送异步事件消息到MQ
     * @param orderDTO
     */
    @Override
    public void createOrderAsync(OrderDTO orderDTO) {
        String stockKey = "item:stock:" + orderDTO.getItemId();
        String stockStr = (String) redisTemplate.opsForValue().get(stockKey);
//        检查库存
        if (stockStr != null) {
            Integer stock = Integer.valueOf(stockStr);
            if (stock <= 0) {
                throw new StockNotEnoughException(MessageConstant.STOCK_NOT_ENOUGH);
            }
        }

        /**
         * 生成一条库存流水
         */
        ItemStockLog itemStockLog = itemService.createItemStockLog(orderDTO.getItemId(), orderDTO.getAmount());

        /**
         * 使用fastJson 序列化对象消息
         * payload 放到消息里 用于消息队列消费
         * args 用于本地事务执行
         */
        JSONObject payload = new JSONObject();
        payload.put("itemId", orderDTO.getItemId());
        payload.put("amount", orderDTO.getAmount());
        payload.put("itemStockLogId",itemStockLog.getId());

        JSONObject args = new JSONObject();
        args.put("itemId", orderDTO.getItemId());
        args.put("amount", orderDTO.getAmount());
        args.put("userId",orderDTO.getUserId());
        args.put("promotionId",orderDTO.getPromotionId());
        payload.put("itemStockLogId",itemStockLog.getId());

        Message message= MessageBuilder.withPayload(payload.toString()).build();
        try {
            TransactionSendResult result =rocketMQTemplate.sendMessageInTransaction("order:decrease_stock",message, args);
            //发送消息到MQ
            if(result.getLocalTransactionState()== LocalTransactionState.ROLLBACK_MESSAGE
                    ||result.getLocalTransactionState()== LocalTransactionState.UNKNOW){
                throw new OrderParamException(MessageConstant.ORDER_ERROR);
            }

        }catch (Exception e){
           throw new OrderParamException(MessageConstant.ORDER_ERROR);
        }

    }
}
