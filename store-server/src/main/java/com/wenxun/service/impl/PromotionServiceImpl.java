package com.wenxun.service.impl;

import com.wenxun.dto.OrderDTO;
import com.wenxun.entity.Item;
import com.wenxun.entity.UserInfo;
import com.wenxun.service.ItemService;
import com.wenxun.service.PromotionService;
import com.wenxun.service.UserService;
import com.wenxun.utils.SnowflakeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author wenxun
 * @date 2024.03.13 21:55
 */
@Service
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserService userService;

    @Autowired
    ItemService itemService;

    /**
     * 先使用大闸 限制获取令牌的数量
     * @param orderDTO userid/itemId/promotionId
     * @return
     */
    @Override
    public String generatePromotionToken(OrderDTO orderDTO) {
        //条件检查
        if(orderDTO.getUserId()<0||orderDTO.getPromotionId()<0||orderDTO.getItemId()<0){
            return null;
        }
        //轻量的检查是否售空/ 如果不经过用户/商品/活动检查 直接减库存的话会多减了
        //库存检查  通过标志位检查 保证原子性
        String stockKey = "item:stock:over:" + orderDTO.getItemId();
        if (redisTemplate.hasKey(stockKey)){
            return null;
        }
        //用户检查
        UserInfo userInfo =userService.getByIdInCache(orderDTO.getUserId());
        if(userInfo==null){
            return null;
        }
        //商品检查
        Item item =  itemService.getByIdInCache(orderDTO.getItemId());
        if(item==null){
            return  null;
        }
        //活动检查 /promotionId/status是否在时间有效期内
        if(item.getPromotion()==null||!item.getPromotion().getId().equals(orderDTO.getPromotionId())||
        !item.getPromotion().checkStatus()){
            return null;
        }
        //发放令牌 先用大闸限流 活动库存10倍发放令牌,令牌通过雪花算法生成
        String grateKey = "promotion:gate"+orderDTO.getPromotionId();

        if(redisTemplate.opsForValue().decrement(grateKey)<0){
            return null;
        }

        String tokenKey = "promotion:token:"+orderDTO.getUserId()+":"+orderDTO.getPromotionId()+":"+orderDTO.getItemId();
        String token = SnowflakeUtils.getId()+"";

        redisTemplate.opsForValue().set(tokenKey,token,10, TimeUnit.MINUTES);

        return token;
    }
}
