package com.wenxun.service;

import com.wenxun.entity.Item;
import io.swagger.models.auth.In;

import java.util.List;

/**
 * @author wenxun
 * @date 2024.03.09 15:25
 */
public interface ItemService {

    /**
     *
     * @param id
     * @return
     */
    Item getById(Integer id);

    Item getByIdInCache(Integer id);

    /**
     * 减库存
     * @param itemId
     * @param amount
     */
    boolean decreaseStock(Integer itemId,Integer amount);

    /**
     * 更新销量
     * @param itemId
     * @param amount
     */
    void updateSales(Integer itemId, Integer amount);

    /**
     * 获取有秒杀活动的商品
     * @return
     */
    List<Item> findItemOnPromotion();
}
