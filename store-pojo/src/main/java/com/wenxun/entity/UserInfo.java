package com.wenxun.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wenxun
 */

@Data
public class UserInfo implements Serializable {

    private Integer id;
    private String phone;
    private String password;
    private String nickname;
    private Byte gender;
    private Integer age;

}