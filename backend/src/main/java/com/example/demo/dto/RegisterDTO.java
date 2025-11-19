// src/main/java/com/example/demo/dto/RegisterDTO.java
package com.example.demo.dto;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * 用户注册DTO（支持手机号或邮箱注册，必须提供验证码）
 */
@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;  // 邮箱（可选，但必须提供邮箱或手机号）

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;  // 手机号（可选，但必须提供邮箱或手机号）

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须是6位数字")
    private String verificationCode;  // 验证码

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    private String nickname;  // 昵称（可选）

    /**
     * 验证：邮箱和手机号至少提供一个
     */
    public boolean isValid() {
        return (email != null && !email.trim().isEmpty()) || 
               (phone != null && !phone.trim().isEmpty());
    }

    /**
     * 获取验证目标（邮箱或手机号）
     */
    public String getVerificationTarget() {
        if (email != null && !email.trim().isEmpty()) {
            return email;
        }
        if (phone != null && !phone.trim().isEmpty()) {
            return phone;
        }
        return null;
    }

    /**
     * 获取验证码类型
     */
    public String getVerificationType() {
        if (email != null && !email.trim().isEmpty()) {
            return "email_register";
        }
        if (phone != null && !phone.trim().isEmpty()) {
            return "sms_register";
        }
        return null;
    }
}

