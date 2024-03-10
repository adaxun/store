package com.wenxun.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wenxun
 */
@Data
public class Item implements Serializable {

    private Integer id;
    private String title;
    private Double price;
    private Integer sales;
    private String imageUrl;
    private String description;
    private ItemStock itemStock;

    /**
     * 设定一个商品只能参加一个活动
     */
    private Promotion promotion;
    private static final long serialVersionUID = 1L;
    public Integer getId() {
        return id;
    }

}