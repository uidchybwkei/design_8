package com.port.interceptor;

import com.port.common.ErrorCode;
import com.port.exception.BusinessException;
import com.port.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或token已过期");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "token无效或已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        request.setAttribute("userId", userId);
        request.setAttribute("username", jwtUtil.getUsernameFromToken(token));

        return true;
    }
}
