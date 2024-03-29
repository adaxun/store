package com.wenxun.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author wenxun
 * @date 2024.03.14 9:45
 */
@Configuration
@Slf4j
public class GuavaConfiguration {

    /**
     * bloom filter 参数
     * capacity : 预期容量
     * errorRate： 可接受错误率
     */
    private long capacity = 1000L;
    private double errorRate = 0.01;
    /**
     * 本地缓存的容量
     */
    private long maximumSize = 1000L;


    /**
     * bloom
     * @return
     */
    @Bean
    public BloomFilter<Integer> itemIdBloomFilter(){
        log.info("开始配置商品id的bloom过滤器");
        BloomFilter<Integer> filter = BloomFilter.create(Funnels.integerFunnel(),capacity,errorRate);
        return filter;
    }

    @Bean
    public Cache<String, Object> itemCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();

    }
}
