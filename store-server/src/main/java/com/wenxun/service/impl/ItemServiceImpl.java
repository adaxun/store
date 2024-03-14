package com.wenxun.service.impl;

import com.google.common.cache.Cache;
import com.google.common.hash.BloomFilter;
import com.wenxun.constant.MessageConstant;
import com.wenxun.entity.Item;
import com.wenxun.entity.ItemStock;
import com.wenxun.entity.Promotion;
import com.wenxun.exception.ItemNotFoundException;
import com.wenxun.mapper.ItemMapper;
import com.wenxun.mapper.ItemStockMapper;
import com.wenxun.mapper.PromotionMapper;
import com.wenxun.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wenxun
 * @date 2024.03.09 15:25
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemStockMapper itemStockMapper;

    @Autowired
    private PromotionMapper promotionMapper;

    @Autowired
    private BloomFilter<Integer> itemIdBloomFilter;

    @Autowired
    private Cache<String,Object> itemCache;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 根据id查询
     * @param id
     * @return
     */
    @Override
    public Item getById(Integer id) {
        /**
         * bloom过滤器检查item是否存在
         */
        if(!itemIdBloomFilter.mightContain(id)){
            throw new ItemNotFoundException(MessageConstant.Item_NOT_FOUND);
        }

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

    /**
     * item 作为热点数据，现在本地guava中查询，查不到再去redis，若查不到再去mysql中
     * @param itemId
     * @return
     */
    @Override
    public Item getByIdInCache(Integer itemId) {
        /**
         * bloom过滤器检查item是否存在
         */
        if(!itemIdBloomFilter.mightContain(itemId)){
            throw new ItemNotFoundException(MessageConstant.Item_NOT_FOUND);
        }
        Item item=null;

        String key = "item:"+itemId;
        //先查guava
        item = (Item) itemCache.getIfPresent(key);
        if(item!=null){
            return  item;
        }
        //再查 redis
        item = (Item) redisTemplate.opsForValue().get(key);
        if(item!=null){
            return item;
        }
        //mysql
        item = getById(itemId);
        if(item!=null){
            itemCache.put(key, item);
            redisTemplate.opsForValue().set(key, item, 3, TimeUnit.MINUTES);
        }
        return item;
    }
}
