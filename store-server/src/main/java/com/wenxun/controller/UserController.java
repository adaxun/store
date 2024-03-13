package com.wenxun.controller;

import com.wenxun.constant.JwtConstant;
import com.wenxun.constant.MessageConstant;
import com.wenxun.dto.UserDTO;
import com.wenxun.entity.UserInfo;
import com.wenxun.properties.JwtProerties;
import com.wenxun.service.UserService;
import com.wenxun.result.Result;
import com.wenxun.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * RestController = ResponseBody:返回序列化Json等格式数据+Controller
 * @author wenxun
 * @date 2024.03.07 10:26
 */
@RequestMapping("/user")
@RestController
@Slf4j
@Api("登录相关接口")
@CrossOrigin(origins = "${frontend.web.path}", allowedHeaders = "*", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtProerties jwtProerties;

    private String generateOTP(){
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for(int i=0;i<4;i++){
            str.append(random.nextInt(10));
        }
        return str.toString();
    }

    /**
     * 生成验证码
     * @param phone
     * @return
     */
    @GetMapping("/otp/{phone}")
    @ApiOperation("获取验证码")
    public Result getOTP(@PathVariable String phone ){

        String otp=generateOTP();
        log.info("手机号：{} 的验证码：{}",phone,otp);
        redisTemplate.opsForValue().set(phone,otp,5, TimeUnit.MINUTES);
        return  Result.success();
    }

    /**
     * 注册
     * @param userDTO
     * @return
     */
    @PostMapping("/register")
    @ApiOperation("注册")
    public  Result register(@RequestBody UserDTO userDTO){
        String realOtp = (String) redisTemplate.opsForValue().get(userDTO.getPhone());
        userDTO.setRealOtp(realOtp);

        log.info("注册：{}",userDTO);
        userService.register(userDTO);
        return Result.success();
    }

    @PostMapping("/login")
    @ApiOperation("登录")
    public Result<String> login(@RequestBody UserDTO userDTO){
        log.info("登录信息：{}",userDTO);
        UserInfo userInfo=userService.login(userDTO);
        HashMap<String,Object> claims = new HashMap<>(1);
        claims.put(JwtConstant.USER_ID,userInfo.getId());
        String token  = JwtUtils.createJWT(jwtProerties.getSecretKey(), jwtProerties.getTtl(), claims);
        log.info("登录用户jwt:{}",token);
        redisTemplate.opsForValue().set(token,userInfo);
        return Result.success(token);
    }

    /**
     * 注销：
     *      客户端： 从session storage 删除token
     *      服务端： 从redis删除缓存的jwt
     * @param token
     * @return
     */
    @GetMapping("/logout")
    @ApiOperation("注销")
    public Result logout( String token){
        if(token!=null) {
            redisTemplate.delete(token);
        }
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("查询状态")
    public  Result<UserInfo> getUser(String token){
        if(token!=null) {
            UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(token);
            log.info("用户状态:{}",userInfo);
            return  Result.success(userInfo);
        }
        return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
    }


}
