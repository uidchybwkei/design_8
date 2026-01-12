package com.port.controller;

import com.port.common.Result;
import com.port.dto.LoginRequest;
import com.port.dto.WxLoginRequest;
import com.port.service.AuthService;
import com.port.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginRequest request) {
        LoginVO loginVO = authService.login(request);
        return Result.success(loginVO);
    }

    @PostMapping("/wx-login")
    public Result<LoginVO> wxLogin(@Validated @RequestBody WxLoginRequest request) {
        LoginVO loginVO = authService.wxLogin(request);
        return Result.success(loginVO);
    }
}
