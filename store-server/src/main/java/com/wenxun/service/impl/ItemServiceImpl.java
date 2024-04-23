package com.wenxun.service.impl;

import com.google.common.cache.Cache;
import com.google.common.hash.BloomFilter;
import com.wenxun.constant.MessageConstant;
import com.wenxun.entity.Item;
import com.wenxun.entity.ItemStock;
import com.wenxun.entity.ItemStockLog;
import com.wenxun.entity.Promotion;
import com.wenxun.exception.ItemNotFoundException;
import com.wenxun.exception.ItemStockLogParamErrorException;
import com.wenxun.exception.OrderParamException;
import com.wenxun.mapper.ItemMapper;
import com.wenxun.mapper.ItemStockLogMapper;
import com.wenxun.mapper.ItemStockMapper;
import com.wenxun.mapper.PromotionMapper;
import com.wenxun.service.ItemService;
import com.wenxun.utils.SnowflakeUtils;
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

    @Autowired
    private ItemStockLogMapper itemStockLogMapper;
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
        if(promotion!=null&&promotion.checkStatus()) {
            item.setPromotion(promotion);
        }
        return item;
    }

    @Override
    public boolean decreaseStock(Integer itemId, Integer amount) {
        return  itemStockMapper.decreaseStock(itemId,amount)==1;
    }

    /**
     * 缓存中扣库存
     *    失败-> 回补到原状态 return false
     *    成功-> if stock==0 设置售空状态
     * @param itemId
     * @param amount
     * @return
     */
    @Override
    public boolean decreaseStockInCache(Integer itemId, Integer amount) {
        if(itemId<0||amount<=0){
            throw new OrderParamException(MessageConstant.ORDER_PARAM_ERROR);
        }
        String stockKey ="item:stock:"+itemId ;
        long flag = redisTemplate.opsForValue().decrement(stockKey,amount);
        // <0 不够-> 扣减失败，还需回滚到扣库存前  ==0 正好售空，redis加入售空标志
        // 1.redis 当前没有开启事务， 导致会出现少卖问题
        // 2.用户秒杀后没付款 但是redis里已经删了库存 所以会存在少卖的问题：可以用reids或者rocketMQ来实现延时队列 回补库存
        if (flag<0){
            increaseStockInCache(itemId,amount);
            return false;
        }
        else if(flag==0){
           redisTemplate.opsForValue().set("item:stock:over:"+itemId,1);
        }
        return true;
    }

    /**
     * 回补redis库存，回滚到扣减前
     * @param itemId
     * @param amount
     * @return
     */
    @Override
    public boolean increaseStockInCache(Integer itemId, Integer amount) {
        if(itemId<=0||amount<=0){
            throw new OrderParamException(MessageConstant.ORDER_PARAM_ERROR);
        }
        String stockKey ="item:stock:"+itemId ;
        redisTemplate.opsForValue().increment(stockKey,amount);
        return true;
    }

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
                    if(promotion.checkStatus()){
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
            itemCache.put(key,item);
            return item;
        }
        //mysql
        item = this.getById(itemId);
        if(item!=null){
            itemCache.put(key, item);
            redisTemplate.opsForValue().set(key, item, 3, TimeUnit.MINUTES);
        }
        return item;
    }

    @Override
    public ItemStockLog createItemStockLog(Integer itemId, Integer amount) {

        if(itemId==null||amount==null||itemId<=0||amount<=0){
            throw new ItemStockLogParamErrorException(MessageConstant.STOCK_LOG_PARAM_ERROR);
        }
        ItemStockLog itemStockLog = new ItemStockLog();
        /**
         * 雪花算法 生成递增趋势的日志，便于mysql插入效率
         */
        String id = String.valueOf(SnowflakeUtils.getId());
        itemStockLog.setId(id);
        itemStockLog.setItemId(itemId);
        itemStockLog.setAmount(amount);
        itemStockLog.setStatus(0);

        //数据库错误会自动拦截
        itemStockLogMapper.insert(itemStockLog);

        return itemStockLog;
    }

    @Override
    public void updateItemStockLog(String id, Integer status) {
        if(id==null){
            throw new ItemStockLogParamErrorException(MessageConstant.STOCK_LOG_PARAM_ERROR);
        }

        itemStockLogMapper.updateStatusById(id, status);
    }

    @Override
    public ItemStockLog getItemStockLogById(String id) {
        if(id==null){
            throw new ItemStockLogParamErrorException(MessageConstant.STOCK_LOG_PARAM_ERROR);
        }
        ItemStockLog itemStockLog = itemStockLogMapper.selectByPrimaryKey(id);
        return itemStockLog;
    }
}
