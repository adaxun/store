package com.wenxun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 开启注解方式的事务管理
 * 开启缓存注解功能
 * @author wenxun
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
public class StoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreApplication.class, args);
    }

}
