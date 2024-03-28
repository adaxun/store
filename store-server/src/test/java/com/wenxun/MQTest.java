package com.wenxun;

import com.wenxun.dto.UserDTO;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * @Test 主线程结束后会自动关闭，所以需要使用main方法启动,需要继承CommandLineRunner,重写run函数
 * @author wenxun
 * @date 2024.03.14 16:55
 */

//@SpringBootApplication
public class MQTest implements CommandLineRunner {
//    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public static void main(String[] args) {
        SpringApplication.run(MQTest.class,args);
    }

    @Override
    public void run(String... args) throws Exception {
        //测试发送异步消息
//        testAsync();

        // 测试事务型消息
        testTransaction();
    }

    public void testAsync() throws Exception {
        for(int i=0;i<10;i++){
            String destination = "testasnc:tag"+i%2;
            Message message = MessageBuilder.withPayload("hello world").build();
            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println("SUCCESS!");
                }

                @Override
                public void onException(Throwable throwable) {
                    System.out.println("error!");
                }
            },3000);

        }
    }

    public void testTransaction() throws Exception {
        for(int i=0;i<10;i++){
            String destination = "testasnc:tag"+i%2;
            Message message = MessageBuilder.withPayload("hello world"+i).build();
            TransactionSendResult result = rocketMQTemplate.sendMessageInTransaction(destination, message,null);
            System.out.println(result);
        }
    }

    @Service
    @RocketMQMessageListener(topic = "testasnc",selectorExpression = "tag0",consumerGroup = "consumer_0")
    public class stringConsumer0 implements RocketMQListener<String> {
        @Override
        public void onMessage(String message) {
            System.out.println("consumer0:"+message);
        }
    }
    @Service
    @RocketMQMessageListener(topic = "testasnc",selectorExpression = "tag1",consumerGroup = "consumer_1")
    public class stringConsumer1 implements RocketMQListener<String> {
        @Override
        public void onMessage(String message) {
            System.out.println("consumer1:"+message);
        }
    }
    @Service
    @RocketMQMessageListener(topic = "testasnc",selectorExpression = "*",consumerGroup = "consumer_x")
    public class stringConsumer2 implements RocketMQListener<String> {
        @Override
        public void onMessage(String message) {
            System.out.println("consumer_x:"+message);
        }
    }

    /**
     * 事务型消息：本地事务
     * 该注解全局只能有一个，所有的事务消息都在此处理 ，否则会冲突
     */
//    @RocketMQTransactionListener
    public class TranscationListenerImpl implements RocketMQLocalTransactionListener {

        /**
         * 执行本地事务
         * @param message
         * @param o
         * @return
         */
        @Override
        public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
            System.out.println("local transaction"+message);
            // 本地执行成功，则提交
            return RocketMQLocalTransactionState.COMMIT;
        }

        /**
         * 检查本地事务
         * @param message
         * @return
         */
        @Override
        public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
            System.out.println("check local transaction"+message);
            return RocketMQLocalTransactionState.COMMIT;
        }
    }
}
