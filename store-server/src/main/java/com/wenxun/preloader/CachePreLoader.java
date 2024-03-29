package com.wenxun.preloader;

import com.wenxun.entity.Item;
import com.wenxun.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 因为下单需要获取大闸和预减库存 所以提前将库存和大闸加载到redis中
 *
 * @author wenxun
 * @date 2024.03.28 16:21
 */

@Component
@Slf4j
public class CachePreLoader {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ItemService itemService;

    @PostConstruct
    public void preloadCache(){
        log.info("开始写入库存缓存&大闸缓存");
        List<Item> itemList = itemService.findItemOnPromotion();
        itemList.forEach(item->{
            int stock =item.getItemStock().getStock() ;
            redisTemplate.opsForValue().set("promotion:gate"+ item.getPromotion().getId(),5*stock);
            redisTemplate.opsForValue().set("item:stock:"+item.getId(),stock);
        });
    }
}
