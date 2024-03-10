package com.wenxun.controller;

import com.wenxun.dto.OrderDTO;
import com.wenxun.entity.UserInfo;
import com.wenxun.result.Result;
import com.wenxun.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @author wenxun
 * @date 2024.03.09 12:46
 */

@RequestMapping("/order")
@RestController
@Api("订单相关接口")
@Slf4j
@CrossOrigin(origins = "${frontend.web.path}", allowedHeaders = "*", allowCredentials = "true")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping("/create")
    @ApiOperation("创建订单")
    public Result create(OrderDTO orderDTO, HttpSession session){
        UserInfo userInfo = (UserInfo) session.getAttribute("loginUser");
        orderDTO.setUserId(userInfo.getId());

        log.info("创建订单");
        orderService.createOrder(orderDTO);
        return Result.success();
    }
}
