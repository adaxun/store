package com.wenxun.exception;

/**
 * @author wenxun
 * @date 2024.03.09 12:23
 */
public class AccountNotFoundException extends BaseException{
    public AccountNotFoundException(String msg){
        super(msg);
    }
}
