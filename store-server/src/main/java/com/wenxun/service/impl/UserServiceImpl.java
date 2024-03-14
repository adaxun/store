package com.wenxun.service.impl;

import com.wenxun.constant.MessageConstant;
import com.wenxun.dto.UserDTO;
import com.wenxun.entity.UserInfo;
import com.wenxun.exception.AccountNotFoundException;
import com.wenxun.exception.DuplicatePhoneException;
import com.wenxun.exception.OptErrorException;
import com.wenxun.exception.PasswordErrorException;
import com.wenxun.mapper.UserInfoMapper;
import com.wenxun.service.UserService;
import com.wenxun.utils.Md5Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author wenxun
 * @date 2024.03.09 9:54
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public UserInfo login(UserDTO userDTO) {
        String phone = userDTO.getPhone();
        String password = userDTO.getPassword();

        UserInfo userInfo = userInfoMapper.selectByPhone(phone);

        if(userInfo==null){
            throw  new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        password = Md5Utils.md5(password);
        if(!password.equals(userInfo.getPassword())){
            throw  new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        return userInfo;
    }

    @Transactional
    @Override
    public void register(UserDTO userDTO) {
        if(userDTO.getOtp()==null||userDTO.getRealOtp()==null
                ||!userDTO.getOtp().equals(userDTO.getRealOtp())){
            throw new OptErrorException(MessageConstant.OPT_ERROR);
        }

        UserInfo userInfo  = new UserInfo();
        BeanUtils.copyProperties(userDTO,userInfo);
        userInfo.setPassword(Md5Utils.md5(userInfo.getPassword()));

        try{
            userInfoMapper.insert(userInfo);
        }catch(DuplicateKeyException e){
            throw  new DuplicatePhoneException(MessageConstant.DUPLICATE_PHONE_ERROR);
        }
    }

    @Override
    public UserInfo getById(Integer id) {
        return  userInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public UserInfo getByIdInCache(Integer id) {
        if(id<0){
            return null;
        }

        String key = "user:"+id;
        UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(key);
        if(userInfo==null){
            userInfo =  userInfoMapper.selectByPrimaryKey(id);
            if(userInfo!=null){
                redisTemplate.opsForValue().set(key,userInfo,30, TimeUnit.MINUTES);
            }
        }
        return userInfo;
    }
}
