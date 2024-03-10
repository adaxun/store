package com.wenxun.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wenxun
 * @date 2024.03.09 11:03
 */
@Data
public class UserDTO implements Serializable {
    private String phone;
    private String password;
    private String nickname;
    private Byte gender;
    private Integer age;
    private String otp;
    private String realOtp;
}