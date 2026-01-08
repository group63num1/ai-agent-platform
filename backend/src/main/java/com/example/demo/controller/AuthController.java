// src/main/java/com/example/demo/controller/AuthController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.*;
import com.example.demo.service.UserService;
import com.example.demo.service.VerificationCodeService;
import com.example.demo.service.UserLoginLogService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.app.entity.Role;
import com.example.demo.app.entity.User;
import com.example.demo.app.mapper.RoleMapper;
import com.example.demo.app.mapper.UserMapper;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private UserLoginLogService userLoginLogService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册（新版本：必须提供验证码）
     * 支持邮箱或手机号注册，必须提供对应的验证码
     */
    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody com.example.demo.dto.RegisterDTO registerDTO) {
        try {
            // 验证：邮箱和手机号至少提供一个
            if (!registerDTO.isValid()) {
                return ApiResponse.fail(400, "必须提供邮箱或手机号");
            }

            // 验证验证码
            String target = registerDTO.getVerificationTarget();
            String type = registerDTO.getVerificationType();
            boolean isValid = verificationCodeService.verifyCode(
                    target, type, registerDTO.getVerificationCode()
            );

            if (!isValid) {
                return ApiResponse.fail(400, "验证码错误或已过期");
            }

            // 转换为UserCreateDTO
            UserCreateDTO userCreateDTO = new UserCreateDTO();
            userCreateDTO.setUsername(registerDTO.getUsername());
            userCreateDTO.setEmail(registerDTO.getEmail());
            userCreateDTO.setPhone(registerDTO.getPhone());
            userCreateDTO.setPassword(registerDTO.getPassword());
            userCreateDTO.setNickname(registerDTO.getNickname());

            // 注册用户
            UserDTO user = userService.register(userCreateDTO);

            // 如果使用邮箱注册，标记邮箱已验证
            if (registerDTO.getEmail() != null && !registerDTO.getEmail().trim().isEmpty()) {
                userMapper.updateEmailVerified(user.getId(), 1);
            }

            // 如果使用手机号注册，标记手机号已验证
            if (registerDTO.getPhone() != null && !registerDTO.getPhone().trim().isEmpty()) {
                userMapper.updatePhoneVerified(user.getId(), 1);
            }

            return ApiResponse.ok(user);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 用户注册（旧版本：兼容性保留，但不推荐使用）
     * @deprecated 请使用新的注册接口，新接口要求验证码验证
     */
    @Deprecated
    @PostMapping("/register/old")
    public ApiResponse<?> registerOld(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        try {
            // 验证：邮箱和手机号至少提供一个
            if (!userCreateDTO.hasEmailOrPhone()) {
                return ApiResponse.fail(400, "必须提供邮箱或手机号");
            }
            return ApiResponse.ok(userService.register(userCreateDTO));
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        try {
            UserDTO user = userService.login(userLoginDTO);

            // 获取用户的角色信息
            List<Role> userRoles = roleMapper.selectByUserId(user.getId());
            List<String> roleCodes = userRoles.stream()
                    .map(Role::getCode)
                    .collect(Collectors.toList());

            // 如果用户没有角色，分配默认角色
            if (roleCodes.isEmpty()) {
                roleCodes.add("user"); // 默认给普通用户角色
            }

            // 生成JWT Token（包含角色信息）
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), roleCodes);
            JwtResponseDTO jwtResponse = new JwtResponseDTO(token, user.getId(), user.getUsername());

            return ApiResponse.ok(jwtResponse);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public ApiResponse<?> checkUsername(@RequestParam String username) {
        try {
            boolean exists = userService.isUsernameExists(username);
            return ApiResponse.ok(exists);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public ApiResponse<?> checkEmail(@RequestParam String email) {
        try {
            boolean exists = userService.isEmailExists(email);
            return ApiResponse.ok(exists);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 邮箱验证码注册
     */
    @PostMapping("/register/email")
    public ApiResponse<?> registerWithEmail(@Valid @RequestBody EmailRegisterDTO registerDTO) {
        try {
            // 验证验证码
            boolean isValid = verificationCodeService.verifyCode(
                    registerDTO.getEmail(),
                    "email_register",
                    registerDTO.getVerificationCode()
            );

            if (!isValid) {
                return ApiResponse.fail(400, "验证码错误或已过期");
            }

            // 创建用户
            UserCreateDTO userCreateDTO = new UserCreateDTO();
            userCreateDTO.setUsername(registerDTO.getEmail().split("@")[0]); // 使用邮箱前缀作为用户名
            userCreateDTO.setEmail(registerDTO.getEmail());
            userCreateDTO.setPassword(registerDTO.getPassword());
            userCreateDTO.setNickname(registerDTO.getNickname());

            UserDTO user = userService.register(userCreateDTO);
            return ApiResponse.ok(user);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 短信验证码登录
     */
    @PostMapping("/login/sms")
    public ApiResponse<?> loginWithSms(@RequestParam String phone,
                                       @RequestParam String verificationCode) {
        try {
            // 验证验证码
            boolean isValid = verificationCodeService.verifyCode(phone, "sms_login", verificationCode);
            if (!isValid) {
                return ApiResponse.fail(400, "验证码错误或已过期");
            }

            // 根据手机号查找用户
            User user = userMapper.selectByPhone(phone);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                throw new RuntimeException("账户已被禁用");
            }

            // 更新最后登录信息
            userMapper.updateLoginInfo(user.getId(), LocalDateTime.now(), "127.0.0.1");

            // 记录登录成功
            userLoginLogService.recordLoginSuccess(
                    user.getId(),
                    "sms",
                    "127.0.0.1",
                    "Web Browser",
                    "PC",
                    "本地"
            );

            // 获取用户角色信息
            List<Role> userRoles = roleMapper.selectByUserId(user.getId());
            List<String> roleCodes = userRoles.stream()
                    .map(Role::getCode)
                    .collect(Collectors.toList());

            // 生成JWT Token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), roleCodes);
            JwtResponseDTO jwtResponse = new JwtResponseDTO(token, user.getId(), user.getUsername());

            return ApiResponse.ok(jwtResponse);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}