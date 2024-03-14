package com.wenxun.service;

import com.wenxun.dto.UserDTO;
import com.wenxun.entity.UserInfo;

/**
 * @author wenxun
 * @date 2024.03.07 10:27
 */
public interface UserService {

    /**
     * 注册
     * @param userDTO
     * @return
     */
    UserInfo login(UserDTO userDTO);

    /**
     * 注册
     * @param userDTO
     */
    void register(UserDTO userDTO);

    /**
     * 根据id获取用户
     * @param id
     * @return
     */
    UserInfo getById(Integer id);

    /**
     * 从redis缓存中获取用户
     * @param id
     * @return
     */
    UserInfo getByIdInCache(Integer id);
}
