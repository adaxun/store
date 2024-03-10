package com.wenxun.service;

import com.wenxun.dto.OrderDTO;

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


}
