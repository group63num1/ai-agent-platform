// src/main/java/com/example/demo/controller/MenuController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.MenuDTO;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户的菜单列表
     * 根据用户角色返回不同的菜单
     */
    @GetMapping
    public ApiResponse<List<MenuDTO>> getMenus(HttpServletRequest request) {
        try {
            // 从请求头中获取 Token
            String token = getJwtFromRequest(request);
            if (token == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            // 从 Token 中获取用户角色
            List<String> roles = jwtUtil.getRolesFromToken(token);
            if (roles == null || roles.isEmpty()) {
                roles = new ArrayList<>();
                roles.add("user"); // 默认角色
            }

            // 判断用户是否为管理员
            boolean isAdmin = roles.contains("admin") || 
                             roles.contains("super_admin") || 
                             roles.contains("ROLE_ADMIN") || 
                             roles.contains("ROLE_SUPER_ADMIN");

            // 根据角色返回不同的菜单
            List<MenuDTO> menus = buildMenus(isAdmin);

            return ApiResponse.ok(menus);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取菜单失败: " + e.getMessage());
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

    /**
     * 构建菜单列表
     * @param isAdmin 是否为管理员
     * @return 菜单列表
     */
    private List<MenuDTO> buildMenus(boolean isAdmin) {
        List<MenuDTO> menus = new ArrayList<>();

        // 首页 - 所有用户都可以看到
        MenuDTO homeMenu = new MenuDTO();
        homeMenu.setTitle("首页");
        homeMenu.setPath("/home");
        homeMenu.setIcon("House");
        homeMenu.setSortOrder(0);
        menus.add(homeMenu);

        // 应用管理 - 所有用户都可以看到
        MenuDTO appsMenu = new MenuDTO();
        appsMenu.setTitle("应用管理");
        appsMenu.setPath("/home/apps");
        appsMenu.setIcon("Grid");
        appsMenu.setSortOrder(1);
        menus.add(appsMenu);

        // 管理员菜单
        if (isAdmin) {
            // 用户管理
            MenuDTO usersMenu = new MenuDTO();
            usersMenu.setTitle("用户管理");
            usersMenu.setPath("/home/users");
            usersMenu.setIcon("User");
            usersMenu.setPermission("user:list");
            usersMenu.setSortOrder(2);
            menus.add(usersMenu);

            // 角色管理
            MenuDTO rolesMenu = new MenuDTO();
            rolesMenu.setTitle("角色管理");
            rolesMenu.setPath("/home/roles");
            rolesMenu.setIcon("UserFilled");
            rolesMenu.setPermission("role:list");
            rolesMenu.setSortOrder(3);
            menus.add(rolesMenu);

            // 权限管理
            MenuDTO permissionsMenu = new MenuDTO();
            permissionsMenu.setTitle("权限管理");
            permissionsMenu.setPath("/home/permissions");
            permissionsMenu.setIcon("Lock");
            permissionsMenu.setPermission("permission:list");
            permissionsMenu.setSortOrder(4);
            menus.add(permissionsMenu);
        }

        // 团队管理 - 所有用户都可以看到
        MenuDTO teamsMenu = new MenuDTO();
        teamsMenu.setTitle("团队管理");
        teamsMenu.setPath("/home/teams");
        teamsMenu.setIcon("OfficeBuilding");
        teamsMenu.setSortOrder(5);
        menus.add(teamsMenu);

        // 个人信息 - 所有用户都可以看到
        MenuDTO profileMenu = new MenuDTO();
        profileMenu.setTitle("个人信息");
        profileMenu.setPath("/home/profile");
        profileMenu.setIcon("User");
        profileMenu.setSortOrder(7);
        menus.add(profileMenu);

        // 系统设置 - 仅管理员可见
        if (isAdmin) {
            MenuDTO systemMenu = new MenuDTO();
            systemMenu.setTitle("系统设置");
            systemMenu.setPath("/home/system");
            systemMenu.setIcon("Setting");
            systemMenu.setPermission("system:config:read");
            systemMenu.setSortOrder(6);

            // 系统设置子菜单
            List<MenuDTO> systemChildren = new ArrayList<>();

            MenuDTO configMenu = new MenuDTO();
            configMenu.setTitle("系统配置");
            configMenu.setPath("/home/system/config");
            configMenu.setIcon(null);
            configMenu.setPermission("system:config:read");
            systemChildren.add(configMenu);

            MenuDTO logsMenu = new MenuDTO();
            logsMenu.setTitle("操作日志");
            logsMenu.setPath("/home/system/logs");
            logsMenu.setIcon(null);
            logsMenu.setPermission("system:log:read");
            systemChildren.add(logsMenu);

            systemMenu.setChildren(systemChildren);
            menus.add(systemMenu);
        }

        return menus;
    }
}

