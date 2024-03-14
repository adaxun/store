package com.wenxun;

import com.wenxun.dto.UserDTO;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wenxun
 * @date 2024.03.14 16:55
 */
public class MQTest {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Test
    public void test()
    {
        UserDTO userDTO = new UserDTO();
        userDTO.setPhone("13800000000");
        userDTO.setNickname("test");

        rocketMQTemplate.convertAndSend("seckillTest:tag1",userDTO);
    }

}

//@Service
//@RocketMQMessageListener(consumerGroup = "consumer1",topic = "seckillTest",selectorExpression ="*")
 class MQTest2 implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {
        System.out.println("StringConsumer0: " + message);
    }


}
