package com.wenxun.exception;

/**
 * @author wenxun
 * @date 2024.03.13 21:47
 */
public class CaptchaWrongException extends BaseException{
    public CaptchaWrongException(String msg){
        super(msg);
    }
}
