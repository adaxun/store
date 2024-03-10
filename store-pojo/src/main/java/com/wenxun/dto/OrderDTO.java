package com.wenxun.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wenxun
 * @date 2024.03.09 12:50
 */
@Data
public class OrderDTO implements Serializable {
    /**
     * itemId 商品id
     * promotionId 商品参与的活动id
     * amount: 下单数量
     * userId：下单用户id
     */
    Integer itemId;
    Integer amount;
    Integer promotionId;

    Integer userId;
}
