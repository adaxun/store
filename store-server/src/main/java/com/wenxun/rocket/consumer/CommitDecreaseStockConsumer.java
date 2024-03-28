package com.wenxun.rocket.consumer;

import com.alibaba.fastjson.JSONObject;
import com.wenxun.constant.MessageConstant;
import com.wenxun.dto.OrderDTO;
import com.wenxun.exception.StockNotEnoughException;
import com.wenxun.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * @author wenxun
 * @date 2024.03.14 22:50
 */
@Service
@RocketMQMessageListener(topic = "order", consumerGroup = "decreaseStockConsumer",selectorExpression = "decreaseStock")
@Slf4j
public class CommitDecreaseStockConsumer implements RocketMQListener<String> {
    @Autowired
    private ItemService itemService;

    /**
     * mqsql减库存
     * @param s
     */
    @Override
    public void onMessage(String s) {
        JSONObject jsonObject = JSONObject.parseObject(s);
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setItemId(jsonObject.getInteger("itemId"));
        orderDTO.setAmount(jsonObject.getInteger("amount"));

        //扣减库存
        boolean flag = itemService.decreaseStock(orderDTO.getItemId(),orderDTO.getAmount());
        if(!flag){
            throw new StockNotEnoughException(MessageConstant.STOCK_NOT_ENOUGH);
        }
        log.info("mysql异步扣减库存，成功");
    }
}
