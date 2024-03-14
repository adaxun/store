package com.wenxun.exception;

/**
 * @author wenxun
 * @date 2024.03.14 16:05
 */
public class RateLimitErrorException extends BaseException{
    public RateLimitErrorException(String msg){
        super(msg);
    }
}
