package com.wenxun.service;

import com.wenxun.dto.OrderDTO;
import com.wenxun.entity.UserInfo;

/**
 * @author wenxun
 * @date 2024.03.13 21:55
 */
public interface PromotionService {
    /**
     * 参加该商品的抢购活动 需要先获取令牌， 通过令牌来限流
     * @param orderDTO
     * @return
     */
    String generatePromotionToken(OrderDTO orderDTO);
}
