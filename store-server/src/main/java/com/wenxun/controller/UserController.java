package com.wenxun.controller;

import com.wenxun.dto.UserDTO;
import com.wenxun.entity.UserInfo;
import com.wenxun.service.UserService;
import com.wenxun.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Random;

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
     * @param session
     * @return
     */
    @GetMapping("/otp/{phone}")
    @ApiOperation("获取验证码")
    public Result getOTP(@PathVariable String phone , HttpSession session){

        String otp=generateOTP();
        log.info("手机号：{} 的验证码：{}",phone,otp);
        session.setAttribute(phone,otp);
        return  Result.success();
    }

    /**
     * 注册
     * @param userDTO
     * @param session
     * @return
     */
    @PostMapping("/register")
    @ApiOperation("注册")
    public  Result register(@RequestBody UserDTO userDTO, HttpSession session){
        String realOtp = (String) session.getAttribute(userDTO.getPhone());
        userDTO.setRealOtp(realOtp);

        log.info("注册：{}",userDTO);
        userService.register(userDTO);
        return Result.success();
    }

    @PostMapping("/login")
    @ApiOperation("登录")
    public Result login(@RequestBody UserDTO userDTO,HttpSession session){
        log.info("登录信息：{}",userDTO);
        UserInfo userInfo=userService.login(userDTO);
        session.setAttribute("loginUser",userInfo);
        return Result.success();
    }

    @GetMapping("/logout")
    @ApiOperation("注销")
    public Result logout(HttpSession session){
        session.invalidate();
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("查询状态")
    public  Result<UserInfo> getUser(HttpSession session){
        UserInfo userInfo = (UserInfo) session.getAttribute("loginUser");
        log.info("用户状态:{}",userInfo);
        return  Result.success(userInfo);
    }


}
