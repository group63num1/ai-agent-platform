// src/main/java/com/example/demo/service/impl/UserServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.dto.UserCreateDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserLoginDTO;
import com.example.demo.app.entity.User;
import com.example.demo.app.entity.UserRole;
import com.example.demo.app.mapper.UserMapper;
import com.example.demo.app.mapper.UserRoleMapper;
import com.example.demo.app.mapper.RoleMapper;
import com.example.demo.app.mapper.TeamMapper;
import com.example.demo.service.UserService;
import com.example.demo.service.UserLoginLogService; // 添加这个导入
import com.example.demo.util.BeanConvertUtil;
import com.example.demo.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private UserLoginLogService userLoginLogService; // 添加这个依赖

    @Autowired
    private TeamMapper teamMapper;

    @Override
    @Transactional
    public UserDTO register(UserCreateDTO userCreateDTO) {
        // 验证：邮箱和手机号至少提供一个
        if (!userCreateDTO.hasEmailOrPhone()) {
            throw new RuntimeException("必须提供邮箱或手机号");
        }

        // 检查用户名是否已存在
        if (isUsernameExists(userCreateDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在（如果提供了邮箱）
        if (userCreateDTO.getEmail() != null && !userCreateDTO.getEmail().trim().isEmpty()) {
            if (isEmailExists(userCreateDTO.getEmail())) {
                throw new RuntimeException("邮箱已被注册");
            }
        }

        // 检查手机号是否已存在（如果提供了手机号）
        if (userCreateDTO.getPhone() != null && !userCreateDTO.getPhone().trim().isEmpty()) {
            User existingUser = userMapper.selectByPhone(userCreateDTO.getPhone());
            if (existingUser != null) {
                throw new RuntimeException("手机号已被注册");
            }
        }

        // 验证密码强度
        if (!passwordUtil.isPasswordValid(userCreateDTO.getPassword())) {
            throw new RuntimeException("密码必须包含字母和数字，且长度至少6位");
        }

        // 创建用户实体
        User user = new User();
        user.setUsername(userCreateDTO.getUsername());
        // 只设置非空的邮箱和手机号
        String email = userCreateDTO.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email.trim());
        }
        String phone = userCreateDTO.getPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            user.setPhone(phone.trim());
        }
        String nickname = userCreateDTO.getNickname();
        if (nickname != null && !nickname.trim().isEmpty()) {
            user.setNickname(nickname.trim());
        }
        user.setPasswordHash(passwordUtil.encodePassword(userCreateDTO.getPassword()));
        user.setStatus(1); // 正常状态
        user.setEmailVerified(0); // 邮箱未验证
        user.setPhoneVerified(0); // 手机未验证
        user.setLoginAttempts(0);

        // 保存用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new RuntimeException("用户注册失败");
        }

        // 为新用户分配默认的"普通用户"角色
        assignDefaultUserRole(user.getId());

        // 返回用户DTO
        return BeanConvertUtil.convert(user, UserDTO.class);
    }

    @Override
    public UserDTO login(UserLoginDTO userLoginDTO) {
        // 根据用户名或邮箱查找用户
        User user = userMapper.selectByUsername(userLoginDTO.getUsername());
        if (user == null) {
            user = userMapper.selectByEmail(userLoginDTO.getUsername());
        }

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用");
        }

        if (user.getStatus() == 2) {
            throw new RuntimeException("账户已被锁定");
        }

        // 验证密码
        if (!passwordUtil.matches(userLoginDTO.getPassword(), user.getPasswordHash())) {
            // 记录登录失败
            userLoginLogService.recordLoginFailure(
                    user.getId(),
                    determineLoginType(userLoginDTO.getUsername()),
                    "127.0.0.1", // 实际应该从HttpServletRequest获取
                    "Web Browser",
                    "密码错误"
            );

            // 增加登录失败次数
            int newAttempts = user.getLoginAttempts() + 1;
            LocalDateTime lockedUntil = null;

            if (newAttempts >= 5) {
                lockedUntil = LocalDateTime.now().plusMinutes(30);
                user.setStatus(2);
            }

            userMapper.updateLoginAttempts(user.getId(), newAttempts, lockedUntil);
            throw new RuntimeException("密码错误");
        }

        // 登录成功，重置登录失败次数
        userMapper.updateLoginAttempts(user.getId(), 0, null);

        // 更新最后登录信息
        userMapper.updateLoginInfo(user.getId(), LocalDateTime.now(), "127.0.0.1");

        // 记录登录成功
        userLoginLogService.recordLoginSuccess(
                user.getId(),
                determineLoginType(userLoginDTO.getUsername()),
                "127.0.0.1",
                "Web Browser",
                "PC",
                "本地"
        );

        // 返回用户信息
        return BeanConvertUtil.convert(user, UserDTO.class);
    }

    /**
     * 根据用户名判断登录类型
     */
    private String determineLoginType(String username) {
        if (username.contains("@")) {
            return "email";
        } else if (username.matches("\\d{11}")) {
            return "phone";
        } else {
            return "username";
        }
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return BeanConvertUtil.convert(user, UserDTO.class);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return BeanConvertUtil.convert(user, UserDTO.class);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userMapper.selectAll();
        return BeanConvertUtil.convertList(users, UserDTO.class);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新用户信息
        existingUser.setNickname(userDTO.getNickname());
        existingUser.setAvatarUrl(userDTO.getAvatarUrl());
        existingUser.setBio(userDTO.getBio());
        existingUser.setPhone(userDTO.getPhone());

        int result = userMapper.update(existingUser);
        if (result <= 0) {
            throw new RuntimeException("更新用户信息失败");
        }

        return BeanConvertUtil.convert(existingUser, UserDTO.class);
    }

    @Override
    public UserDTO updateUserAvatar(Long id, String avatarUrl) {
        User existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 如果有旧头像，删除旧文件
        if (existingUser.getAvatarUrl() != null && !existingUser.getAvatarUrl().isEmpty()) {
            // 这里可以调用文件删除逻辑
            System.out.println("删除旧头像: " + existingUser.getAvatarUrl());
        }

        existingUser.setAvatarUrl(avatarUrl);
        int result = userMapper.update(existingUser);
        if (result <= 0) {
            throw new RuntimeException("更新头像失败");
        }

        return BeanConvertUtil.convert(existingUser, UserDTO.class);
    }

    @Override
    public void deleteUser(Long id) {
        int result = userMapper.softDelete(id);
        if (result <= 0) {
            throw new RuntimeException("删除用户失败");
        }
    }

    @Override
    @Transactional
    public void hardDeleteUser(Long id) {
        // 检查用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户是否是团队负责人
        // 由于 teams 表的 owner_id 设置了 ON DELETE RESTRICT，如果用户是团队负责人，删除会失败
        List<com.example.demo.app.entity.Team> ownedTeams = teamMapper.selectByOwnerId(id);
        if (ownedTeams != null && !ownedTeams.isEmpty()) {
            throw new RuntimeException("无法注销账户：您仍然是 " + ownedTeams.size() + " 个团队的负责人。请先转移团队负责人身份或删除相关团队后再注销账户。");
        }

        // 注意：由于数据库外键约束设置：
        // - user_roles, user_login_logs, user_verification_codes 等表设置了 ON DELETE CASCADE，会自动删除
        // - teams 表的 owner_id 设置了 ON DELETE RESTRICT（已在上方检查）
        // - operation_logs 表的 user_id 设置了 ON DELETE SET NULL，会自动设置为NULL
        
        // 先删除用户角色关联（虽然CASCADE会自动删除，但为了确保事务一致性，先手动删除）
        userRoleMapper.deleteByUserId(id);

        // 硬删除用户（从数据库彻底删除）
        int result = userMapper.hardDelete(id);
        if (result <= 0) {
            throw new RuntimeException("删除用户失败");
        }
    }

    @Override
    @Transactional
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        // 先删除用户现有的角色
        userRoleMapper.deleteByUserId(userId);

        // 添加新的角色
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setAssignedBy(userId); // 自己分配给自己
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    public boolean isUsernameExists(String username) {
        User user = userMapper.selectByUsername(username);
        return user != null;
    }

    @Override
    public boolean isEmailExists(String email) {
        User user = userMapper.selectByEmail(email);
        return user != null;
    }

    /**
     * 为新用户分配默认的"普通用户"角色
     */
    private void assignDefaultUserRole(Long userId) {
        try {
            // 查找"普通用户"角色的ID（code为"user"）
            var defaultRole = roleMapper.selectByCode("user");
            if (defaultRole != null) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(defaultRole.getId());
                userRole.setAssignedBy(userId); // 系统自动分配
                userRoleMapper.insert(userRole);
            } else {
                System.err.println("警告：未找到默认用户角色，用户 " + userId + " 将没有角色");
            }
        } catch (Exception e) {
            System.err.println("分配默认角色失败: " + e.getMessage());
        }
    }
}