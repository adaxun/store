package com.wenxun.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * 生成jwt和解密jwt
 * @author wenxun
 * @date 2024.03.13 14:48
 */
public class JwtUtils {
    /**
     * 生成jwt
     * @param secretKey
     * @param ttlMillis
     * @param claims
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String,Object> claims){
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // 过期时间设定
        long expMillis = System.currentTimeMillis()+ttlMillis;
        Date exp = new Date(expMillis);

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .signWith(signatureAlgorithm,secretKey.getBytes(StandardCharsets.UTF_8))
                .setExpiration(exp);
        return builder.compact();
    }

    /**
     * 解密
     * @param secretKey
     * @param token
     * @return
     */
    public  static Claims parseJWT(String secretKey , String token){
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token).getBody();

        return claims;
    }
}
