package com.wenxun.handler;

import com.wenxun.constant.MessageConstant;
import com.wenxun.exception.BaseException;
import com.wenxun.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 捕获全局异常
 * @author wenxun
 * @date 2024.03.07 10:16
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 常规业务异常
     * @param baseException
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException baseException){
        log.error("异常信息：{}",baseException.getMessage());
        return Result.error(baseException.getMessage());
    }

    /**
     * SQL异常 特判组主键已存在的异常
     * @param sqlIntegrityConstraintViolationException
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException sqlIntegrityConstraintViolationException){
        String message = sqlIntegrityConstraintViolationException.getMessage();
        if(message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String username = split[2];
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }else{
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
