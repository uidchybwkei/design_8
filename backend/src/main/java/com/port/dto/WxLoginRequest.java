package com.port.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WxLoginRequest {
    @NotBlank(message = "微信code不能为空")
    private String code;

    // Explicit getters and setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
