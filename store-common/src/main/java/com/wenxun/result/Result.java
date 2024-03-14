package com.wenxun.result;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wenxun
 * @date 2024.03.07 10:18
 */
@Data
public class Result<T> implements Serializable {
    /**
     * code: 1成功
     * msg： 错误信息
     * data: 数据
     */
    private Integer code;
    private  String msg;
    private  T data;

    public static <T> Result<T> success(){
        Result<T> result = new Result<>();
        result.code =0 ;
        return result;
    }

    public static <T> Result<T> success(T object){
        Result<T> result = new Result<T>();
        result.data=object;
        result.code=0;
        return  result;
    }

    public static <T> Result<T>  error(String msg){
        Result result = new Result();
        result.msg = msg;
        result.code =1;
        return result;
    }
    public static <T> Result<T>  error(T object){
        Result result = new Result();
        result.data = object;
        result.code =1;
        return result;
    }
}
