package com.wenxun.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 默认的序列化方法是JdkSerializationRedisSerializer，会序列化为二进制，StringRedisSerializer可以
 * 序列化为字符，但是不能对复杂数据结构序列化
 * @author wenxun
 * @date 2024.03.13 15:58
 */
@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate (RedisConnectionFactory redisConnectionFactory){
        log.info("配置reids模板对象");
        RedisTemplate redisTemplate = new RedisTemplate();

        //设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis key的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
