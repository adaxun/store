package com.wenxun.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ItemStock implements Serializable {

    private Integer id;
    private Integer itemId;
    private Integer stock;
}