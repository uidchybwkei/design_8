package com.port.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String realName;

    // Explicit constructor
    public LoginVO(String token, Long userId, String username, String realName) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.realName = realName;
    }

    // Explicit getters and setters for Jackson serialization
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
}
