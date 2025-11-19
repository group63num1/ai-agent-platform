// src/main/java/com/example/demo/controller/AdminController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    /**
     * 模拟管理员操作：删除用户
     * 仅管理员可以访问
     * 接口路径：POST /api/v1/admin/delete-user
     */
    @PostMapping("/delete-user")
    public ApiResponse<String> deleteUser(@RequestParam Long userId, HttpServletRequest request) {
        // 这个接口需要管理员权限，会由Spring Security拦截
        // 如果是普通用户访问，会返回403错误
        return ApiResponse.ok("用户删除成功");
    }
}

