// src/main/java/com/example/demo/dto/UserCreateDTO.java
package com.example.demo.dto;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class UserCreateDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;  // 邮箱（可选，但必须提供邮箱或手机号）

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;  // 手机号（可选，但必须提供邮箱或手机号）

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    private String nickname;

    /**
     * 验证：邮箱和手机号至少提供一个
     */
    public boolean hasEmailOrPhone() {
        return (email != null && !email.trim().isEmpty()) || 
               (phone != null && !phone.trim().isEmpty());
    }
}