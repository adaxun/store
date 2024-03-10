package com.wenxun.entity;

import lombok.Data;

import java.io.Serializable;


@Data
public class ItemStockLog implements Serializable {

    private String id;
    private Integer itemId;
    private Integer amount;
    private Integer status;
}