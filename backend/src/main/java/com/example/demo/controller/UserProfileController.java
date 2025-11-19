// src/main/java/com/example/demo/controller/UserProfileController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/user")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户的个人信息
     * 接口路径：GET /api/v1/user/profile
     */
    @GetMapping("/profile")
    public ApiResponse<UserDTO> getUserProfile(HttpServletRequest request) {
        try {
            String token = getJwtFromRequest(request);
            if (token == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            Long currentUserId = jwtUtil.getUserIdFromToken(token);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            UserDTO user = userService.getUserById(currentUserId);
            return ApiResponse.ok(user);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取个人信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新当前用户的个人信息
     * 接口路径：PUT /api/v1/user/profile
     */
    @PutMapping("/profile")
    public ApiResponse<UserDTO> updateUserProfile(HttpServletRequest request, @RequestBody UserDTO userDTO) {
        try {
            String token = getJwtFromRequest(request);
            if (token == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            Long currentUserId = jwtUtil.getUserIdFromToken(token);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            // 用户只能更新自己的信息
            UserDTO updatedUser = userService.updateUser(currentUserId, userDTO);
            return ApiResponse.ok(updatedUser);
        } catch (Exception e) {
            return ApiResponse.fail(400, "更新个人信息失败: " + e.getMessage());
        }
    }

    /**
     * 注销当前用户账户（硬删除，从数据库彻底删除）
     * 接口路径：DELETE /api/v1/user/profile
     */
    @DeleteMapping("/profile")
    public ApiResponse<String> deleteUserProfile(HttpServletRequest request) {
        try {
            String token = getJwtFromRequest(request);
            if (token == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            Long currentUserId = jwtUtil.getUserIdFromToken(token);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            // 用户只能注销自己的账户，使用硬删除从数据库彻底删除
            userService.hardDeleteUser(currentUserId);
            return ApiResponse.ok("账户注销成功，数据已从数据库彻底删除");
        } catch (Exception e) {
            return ApiResponse.fail(400, "注销账户失败: " + e.getMessage());
        }
    }

    /**
     * 从请求中获取 JWT Token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
