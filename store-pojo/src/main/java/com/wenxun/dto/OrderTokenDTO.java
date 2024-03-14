package com.wenxun.dto;

import lombok.Data;

/**
 * @author wenxun
 * @date 2024.03.14 16:13
 */
@Data
public class OrderTokenDTO extends OrderDTO{
    public String promotionToken;
}
