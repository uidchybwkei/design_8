package com.port.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.port.common.ErrorCode;
import com.port.dto.LoginRequest;
import com.port.dto.WxLoginRequest;
import com.port.entity.User;
import com.port.exception.BusinessException;
import com.port.mapper.UserMapper;
import com.port.util.JwtUtil;
import com.port.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginVO login(LoginRequest request) {
        String hashedPassword = DigestUtil.md5Hex(request.getPassword());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername())
                .eq(User::getPassword, hashedPassword)
                .eq(User::getStatus, 1);

        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginVO(token, user.getId(), user.getUsername(), user.getRealName());
    }

    public LoginVO wxLogin(WxLoginRequest request) {
        String openid = "mock_openid_" + request.getCode();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid)
                .eq(User::getStatus, 1);

        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未绑定，请先在后台创建账号并绑定openid");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginVO(token, user.getId(), user.getUsername(), user.getRealName());
    }
}
