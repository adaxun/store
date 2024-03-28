package com.wenxun.rocket.consumer;

import com.alibaba.fastjson.JSONObject;
import com.wenxun.service.ItemService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wenxun
 * @date 2024.03.14 22:50
 */
@Service
@RocketMQMessageListener(topic = "order", consumerGroup = "increaseSalesConsumer",selectorExpression = "increaseSales")
public class RollbakIncreaseSalesConsumer implements RocketMQListener<String> {

    @Autowired
    private ItemService itemService;

    @Override
    public void onMessage(String s) {
        JSONObject jsonObject = JSONObject.parseObject(s);
        //更新销量
        itemService.updateSales(jsonObject.getInteger("itemId"), jsonObject.getInteger("amount"));
    }
}
