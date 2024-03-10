package com.wenxun.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SerialNumber implements Serializable {

    private String name;

    private Integer value;
    private Integer step;

}