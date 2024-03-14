package com.wenxun.controller;

import com.wenxun.constant.MessageConstant;
import com.wenxun.dto.OrderDTO;
import com.wenxun.dto.OrderTokenDTO;
import com.wenxun.entity.UserInfo;
import com.wenxun.exception.CaptchaWrongException;
import com.wenxun.exception.PromotionErrorException;
import com.wenxun.result.Result;
import com.wenxun.service.OrderService;
import com.wenxun.service.PromotionService;
import com.wf.captcha.ArithmeticCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

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
    private OrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private  PromotionService promotionService;

    @PostMapping("/create{token}")
    @ApiOperation("创建订单")
    public Result create(@PathVariable String token, @RequestBody OrderTokenDTO orderDTO){
        UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(token);
        orderDTO.setUserId(userInfo.getId());
        log.info("创建订单:{}",orderDTO);
        orderService.prepareOrder(orderDTO);
        return Result.success();
    }

    @GetMapping("/captcha")
    @ApiOperation("获取验证码")
    public void captcha(String token,HttpServletResponse httpServletResponse){

        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130,48);
        captcha.setLen(2);
        log.info("生成验证码，答案为:{}",captcha.text());

        if(token!=null){
            UserInfo userInfo =(UserInfo) redisTemplate.opsForValue().get(token);
            if(userInfo!=null){
                String captchaKey = "captcha:"+userInfo.getId().toString();
                redisTemplate.opsForValue().set(captchaKey,captcha.text(),1, TimeUnit.MINUTES);
            }
        }
        httpServletResponse.setContentType("image/png");
        try{
            OutputStream os = httpServletResponse.getOutputStream();
            captcha.out(os);
        }catch (IOException e){
            log.error("验证码返送失败：{}",e.getMessage());
        }
    }

    @PostMapping("/token{token}")
    @ApiOperation("验证码校验+通行令牌")
    public Result<String> token(@PathVariable String token ,@RequestBody OrderDTO orderDTO){
        if(token==null){
            throw  new CaptchaWrongException(MessageConstant.CAPTCHA_ERROR);
        }
        UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(token);
        String captcha = redisTemplate.opsForValue().get("captcha:"+userInfo.getId().toString()).toString();
        if( !token.equals(captcha)){
            throw new CaptchaWrongException(MessageConstant.CAPTCHA_ERROR);
        }
        /**
         * 令牌桶 限流
         */
        orderDTO.setUserId(userInfo.getId());
        String promotionToken = promotionService.generatePromotionToken(orderDTO);
        if(promotionToken==null){
            throw new PromotionErrorException(MessageConstant.PROMOTION_TOKEN_ERROR);
        }
        return Result.success(promotionToken);
    }
}
