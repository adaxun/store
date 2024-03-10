package com.wenxun.service.impl;

import com.wenxun.entity.Item;
import com.wenxun.entity.ItemStock;
import com.wenxun.entity.Promotion;
import com.wenxun.mapper.ItemMapper;
import com.wenxun.mapper.ItemStockMapper;
import com.wenxun.mapper.PromotionMapper;
import com.wenxun.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wenxun
 * @date 2024.03.09 15:25
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    ItemMapper itemMapper;

    @Autowired
    ItemStockMapper itemStockMapper;

    @Autowired
    PromotionMapper promotionMapper;

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @Override
    public Item getById(Integer id) {
        Item item = itemMapper.selectByPrimaryKey(id);

        ItemStock itemStock = itemStockMapper.selectByItemId(id);
        item.setItemStock(itemStock);

        Promotion promotion = promotionMapper.selectByItemId(id);
        if(promotion!=null&&promotion.getStatus()) {
            item.setPromotion(promotion);
        }
        return item;
    }

    @Transactional
    @Override
    public boolean decreaseStock(Integer itemId, Integer amount) {
        return  itemStockMapper.decreaseStock(itemId,amount)==1;
    }

    @Transactional
    @Override
    public void updateSales(Integer itemId, Integer amount) {
        itemMapper.increaseSales(itemId,amount);
    }

    /**
     * 先用最笨的方法实现
     * @return
     */
    @Override
    public List<Item> findItemOnPromotion() {
        List<Item> itemList = itemMapper.selectOnPromotion();

        return itemList.stream().map(
                item -> {
                    ItemStock itemStock = itemStockMapper.selectByItemId(item.getId());
                    item.setItemStock(itemStock);

                    Promotion promotion = promotionMapper.selectByItemId(item.getId());
                    if(promotion.getStatus()){
                        item.setPromotion(promotion);
                    }
                    return item;
                }
        ).collect(Collectors.toList());
    }
}
