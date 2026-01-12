package com.port.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.port.entity.User;
import com.port.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    public User getUserInfo(Long userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, userId)
                .eq(User::getStatus, 1);
        return userMapper.selectOne(wrapper);
    }
}
