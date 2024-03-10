package com.wenxun.service.impl;

import com.wenxun.constant.MessageConstant;
import com.wenxun.dto.OrderDTO;
import com.wenxun.entity.*;
import com.wenxun.exception.*;
import com.wenxun.mapper.OrderInfoMapper;
import com.wenxun.mapper.SerialNumberMapper;
import com.wenxun.service.ItemService;
import com.wenxun.service.OrderService;
import com.wenxun.service.UserService;
import com.wenxun.utils.TimeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author wenxun
 * @date 2024.03.09 12:55
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    UserService userService;

    @Autowired
    ItemService itemService;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    SerialNumberMapper serialNumberMapper;

    /**
     * 生成订单id 日期+index： 20240309+000000000001 total：20位
     * 便于根据日期 分表
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderId(){
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


    @Transactional
    @Override
    public void createOrder(OrderDTO orderDTO) {

        //校验下单参数合法性
        if(orderDTO.getAmount()==null || orderDTO.getAmount()<1||orderDTO.getItemId()==null
        ||orderDTO.getUserId()==null||orderDTO.getPromotionId()==null){
            throw new OrderParamException(MessageConstant.ORDER_PARAM_ERROR);
        }
        //校验用户
        UserInfo userInfo = userService.getById(orderDTO.getUserId());
        if(userInfo==null){
            throw  new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        //校验商品
        Item item = itemService.getById(orderDTO.getItemId());
        if(item==null){
            throw  new ItemNotFoundException(MessageConstant.Item_NOT_FOUND);
        }
        //校验库存
        Integer stock = item.getItemStock().getStock();
        if(stock<orderDTO.getAmount()){
            throw new StockNotEnoughException(MessageConstant.STOCK_NOT_ENOUGH);
        }

        //校验活动：是否存在/时间满足条件
        Promotion promotion = item.getPromotion();
        if(promotion==null){
            throw new PromotionErrorException(MessageConstant.PROMOTION_NOT_FOUND);
        }
        else if(!promotion.getStatus()){
            throw  new PromotionErrorException(MessageConstant.PROMOTION_TIME_ERROR);
        }

        //扣减库存
        boolean flag = itemService.decreaseStock(orderDTO.getItemId(),orderDTO.getAmount());
        if(!flag){
            throw new StockNotEnoughException(MessageConstant.STOCK_NOT_ENOUGH);
        }

        //生成订单
        OrderInfo orderInfo =new OrderInfo();
        orderInfo.setId(this.generateOrderId());

        BeanUtils.copyProperties(orderDTO,orderInfo);
        orderInfo.setOrderAmount(orderDTO.getAmount());
        orderInfo.setOrderPrice(promotion.getPromotionPrice());
        orderInfo.setOrderTime(new Timestamp(System.currentTimeMillis()));

        BigDecimal total = orderInfo.getOrderPrice().multiply(new BigDecimal(orderDTO.getAmount()));
        orderInfo.setOrderTotal(total);

        orderInfoMapper.insert(orderInfo);

        itemService.updateSales(orderDTO.getItemId(),orderDTO.getAmount());
    }
}
