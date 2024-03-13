package com.wenxun.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author wenxun
 * @date 2024.03.13 14:35
 */
@Component
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProerties {

    /**
     * 密钥 过期时间 前端传过来的jwt名称
     */
    private  String secretKey;
    private long ttl;
    private String frontEndTokenName;
}
