package com.wenxun.rocket.producer;

import com.alibaba.fastjson.JSONObject;
import com.wenxun.dto.OrderDTO;
import com.wenxun.entity.ItemStockLog;
import com.wenxun.service.ItemService;
import com.wenxun.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * 事务型消息 只能在这里处理，因为@RocketMQTransactionListener全局只能配置一个，根据top和tag来区别业务
 * @author wenxun
 * @date 2024.03.14 22:47
 */


@Service
@RocketMQTransactionListener
@Slf4j
public class TransactionListenerImpl implements RocketMQLocalTransactionListener {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ItemService itemService;
    /**
     * 本地事务的实现
     * @param message
     * @param o
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {

        try {
            String tage = message.getHeaders().get("rocketmq_TAGS").toString();
            //创建订单
            if("decreaseStock".equals(tage)){
                return this.createOrder(message,o);
            }




            return RocketMQLocalTransactionState.UNKNOWN;
        }catch (Exception e){
            log.error("消息队列，执行本地事务出错:"+e.getMessage());
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 本地事务的check
     * @param message
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        try {
            String tage = message.getHeaders().get("rocketmq_TAGS").toString();
            //创建订单
            if("decreaseStock".equals(tage)){
                return this.checkStockLog(message);
            }



            return RocketMQLocalTransactionState.UNKNOWN;
        }catch (Exception e){
            log.error("消息队列，执行本地事务出错:"+e.getMessage());
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 本地事务执行
     * @param message
     * @param o
     * @return
     */
    private RocketMQLocalTransactionState createOrder(Message message, Object o){
        JSONObject param = (JSONObject)o;
        //首先取出消息
        OrderDTO orderDTO = new OrderDTO();
        String itemStockLogId = (String) param.get("itemStockLogId");
        orderDTO.setItemId(param.getInteger("itemId"));
        orderDTO.setAmount(param.getInteger("amount"));
        orderDTO.setUserId(param.getInteger("userId"));

        try {
            orderService.createOrder(orderDTO);
            //更新库存流水
            log.info("消息队列，本地事务-创建订单成功");
            itemService.updateItemStockLog(itemStockLogId,1);
            return RocketMQLocalTransactionState.COMMIT;

        }catch (Exception e){
            log.error("消息队列，本地事务-创建订单出错:"+e.getMessage());
            itemService.updateItemStockLog(itemStockLogId,2);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * check本地事务执行结果
     * @param message 传过来的也是给 异步减库存的消息
     * @return
     */
    private RocketMQLocalTransactionState checkStockLog(Message message){
        JSONObject param = (JSONObject)message.getPayload();
        String itemStockLogId = (String) param.get("itemStockLogId");
        ItemStockLog itemStockLog = itemService.getItemStockLogById(itemStockLogId);

        if(itemStockLog==null||itemStockLog.getStatus()==3){
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        else if(itemStockLog.getStatus()==1){
            return RocketMQLocalTransactionState.COMMIT;
        }
        return RocketMQLocalTransactionState.UNKNOWN;

    }
}
