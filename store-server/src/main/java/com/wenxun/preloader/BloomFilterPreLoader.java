package com.wenxun.preloader;

import com.google.common.hash.BloomFilter;
import com.wenxun.entity.Item;
import com.wenxun.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Guava的bloomFilter没有持久化 每次重启需要提前加载
 * @author wenxun
 * @date 2024.03.28 16:23
 */
@Component
@Slf4j
public class BloomFilterPreLoader {


    @Autowired
    private BloomFilter<Integer> itemIdBloomFilter;

    @Autowired
    private ItemService itemService;
    @PostConstruct
    public void preloadFilter(){
        log.info("开始写入Bloom过滤器");
        List<Item> itemList = itemService.findItemOnPromotion();
        itemList.forEach(item -> {
            itemIdBloomFilter.put(item.getId());
        });

    }
}
