package com.port.controller;

import com.port.common.Result;
import com.port.entity.User;
import com.port.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestAttribute("userId") Long userId) {
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }
}
