package com.wenxun.controller;

import com.wenxun.entity.Item;
import com.wenxun.result.Result;
import com.wenxun.service.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wenxun
 * @date 2024.03.09 12:47
 */

@RestController
@RequestMapping("/item")
@Api("商品相关接口")
@Slf4j
@CrossOrigin(origins = "${frontend.web.path}", allowedHeaders = "*", allowCredentials = "true")
public class  ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping("/list")
    @ApiOperation("获取参加活动的所有商品")
    Result<List<Item>> getItemList(){
        log.info("获取参加活动的商品");
        return Result.success(itemService.findItemOnPromotion());
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("获取某个id的详情")
    Result<Item> getById(@PathVariable Integer id){
        log.info("获取id为：{}的商品",id);
        return Result.success(itemService.getById(id));
    }

}
