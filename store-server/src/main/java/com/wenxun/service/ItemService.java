package com.wenxun.service;

import com.wenxun.entity.Item;
import com.wenxun.entity.ItemStockLog;
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

    /**
     * 创建订单流水
     * @param itemId
     * @param amount
     * @return
     */
    ItemStockLog createItemStockLog(Integer itemId,Integer amount);

    /**
     * 根据id更新流水状态
     * @param id
     * @param status
     */
    void updateItemStockLog(String id,Integer status);

    /**
     * 根据id获取流水
     * @param id
     * @return
     */
    ItemStockLog getItemStockLogById(String id);
}
