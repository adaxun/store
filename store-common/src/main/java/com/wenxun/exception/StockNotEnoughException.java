package com.wenxun.exception;

/**
 * @author wenxun
 * @date 2024.03.09 15:05
 */
public class StockNotEnoughException extends  BaseException{
    public  StockNotEnoughException(String msg){
        super(msg);
    }
}
