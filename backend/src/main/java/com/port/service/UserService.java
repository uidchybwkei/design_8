package com.port.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.port.entity.User;
import com.port.exception.BusinessException;
import com.port.mapper.UserMapper;
import com.port.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    public User getUserInfo(Long userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, userId)
                .eq(User::getStatus, 1);
        return userMapper.selectOne(wrapper);
    }

    public User getCurrentUser(String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = getUserInfo(userId);
            if (user == null) {
                throw new BusinessException("用户不存在或已禁用");
            }
            return user;
        } catch (Exception e) {
            throw new BusinessException("无效的登录状态，请重新登录");
        }
    }
}
