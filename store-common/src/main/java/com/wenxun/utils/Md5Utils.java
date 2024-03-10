package com.wenxun.utils;

import com.wenxun.constant.MessageConstant;
import com.wenxun.exception.PasswordErrorException;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author wenxun
 * @date 2024.03.10 9:17
 */
public class Md5Utils {
    private static  final  String salt = "fjalksd";

    public  static String md5(String str){
        if(str==null||str.length()==0){
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        return DigestUtils.md5DigestAsHex((str+salt).getBytes(StandardCharsets.UTF_8));
    }
}
