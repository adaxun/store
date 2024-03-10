package com.wenxun.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderInfo implements Serializable {

    private String id;

    private Integer userId;

    private Integer itemId;

    private Integer promotionId;

    private BigDecimal orderPrice;

    private Integer orderAmount;

    private BigDecimal orderTotal;

    private Date orderTime;


}