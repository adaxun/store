package com.wenxun.interceptor;

import com.wenxun.constant.JwtConstant;
import com.wenxun.properties.JwtProerties;
import com.wenxun.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wenxun
 * @date 2024.03.13 14:42
 */
@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProerties jwtProerties;

    /**
     * 静态资源不拦截，拦截token不通过的
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if( !(handler instanceof HandlerMethod)){
            return true;
        }

        String token = request.getHeader(jwtProerties.getFrontEndTokenName());

        try {
            Claims claims = JwtUtils.parseJWT(jwtProerties.getSecretKey(),token);
            Integer userId = Integer.valueOf(claims.get(JwtConstant.USER_ID).toString());
            log.info("登录用户的id:{}",userId);
            return true;
        }
        catch (Exception e){
            response.setStatus(401);
            return false;
        }



    }
}
