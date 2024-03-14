package com.wenxun.service;

import com.wenxun.dto.OrderDTO;
import com.wenxun.dto.OrderTokenDTO;

/**
 * @author wenxun
 * @date 2024.03.09 12:54
 */
public interface OrderService {
    /**
     * 创建订单
     * @param orderDTO
     */
    void createOrder(OrderDTO orderDTO);

    /**
     * 创建订单前的 令牌桶限流
     * @param orderDTO
     */
    void prepareOrder(OrderTokenDTO orderDTO);

    /**
     * 异步扣减库存实现下单
     * @param orderDTO
     */
    void createOrderAsync(OrderDTO orderDTO);
}
