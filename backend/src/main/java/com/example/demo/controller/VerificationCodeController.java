// src/main/java/com/example/demo/controller/VerificationCodeController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/verification")
public class VerificationCodeController {

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 发送邮箱注册验证码
     */
    @PostMapping("/email/register")
    public ApiResponse<Void> sendEmailRegisterCode(@RequestParam String email) {
        try {
            verificationCodeService.sendEmailCode(email, "email_register", null);
            // 开发模式提示：验证码会在控制台输出
            return new ApiResponse<>(0, "验证码已发送（开发模式：请查看后端控制台输出）", null);
        } catch (Exception e) {
            // 记录详细错误信息
            System.err.println("发送邮箱注册验证码失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.fail(500, "验证码发送失败: " + e.getMessage());
        }
    }

    /**
     * 发送邮箱密码重置验证码
     */
    @PostMapping("/email/reset-password")
    public ApiResponse<Void> sendEmailResetCode(@RequestParam String email) {
        try {
            verificationCodeService.sendEmailCode(email, "email_reset", null);
            // 开发模式提示：验证码会在控制台输出
            return new ApiResponse<>(0, "验证码已发送（开发模式：请查看后端控制台输出）", null);
        } catch (Exception e) {
            System.err.println("发送邮箱密码重置验证码失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.fail(500, "验证码发送失败: " + e.getMessage());
        }
    }

    /**
     * 发送短信登录验证码
     */
    @PostMapping("/sms/login")
    public ApiResponse<Void> sendSmsLoginCode(@RequestParam String phone) {
        try {
            boolean success = verificationCodeService.sendSmsCode(phone, "sms_login", null);
            if (success) {
                return new ApiResponse<>(0, "验证码已发送到手机", null);
            } else {
                return ApiResponse.fail(500, "验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 发送手机号注册验证码
     */
    @PostMapping("/sms/register")
    public ApiResponse<Void> sendSmsRegisterCode(@RequestParam String phone) {
        try {
            verificationCodeService.sendSmsCode(phone, "sms_register", null);
            // 开发模式提示：验证码会在控制台输出
            return new ApiResponse<>(0, "验证码已发送（开发模式：请查看控制台输出）", null);
        } catch (Exception e) {
            // 记录详细错误信息
            System.err.println("发送手机号注册验证码失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.fail(500, "验证码发送失败: " + e.getMessage());
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify")
    public ApiResponse<Void> verifyCode(@RequestParam String target,
                                        @RequestParam String type,
                                        @RequestParam String code) {
        try {
            boolean isValid = verificationCodeService.verifyCode(target, type, code);
            if (isValid) {
                return new ApiResponse<>(0, "验证码验证成功", null);
            } else {
                return ApiResponse.fail(400, "验证码错误或已过期");
            }
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
