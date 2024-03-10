package com.wenxun.exception;

/**
 * @author wenxun
 * @date 2024.03.09 9:53
 */
public class BaseException extends RuntimeException{
    BaseException(String msg){
        super(msg);
    }
    BaseException(){

    }
}
