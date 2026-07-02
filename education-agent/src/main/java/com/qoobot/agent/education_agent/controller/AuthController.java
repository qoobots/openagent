package com.qoobot.agent.education_agent.controller;

import com.qoobot.agent.education_agent.common.Result;
import com.qoobot.agent.education_agent.dto.LoginRequest;
import com.qoobot.agent.education_agent.dto.RegisterRequest;
import com.qoobot.agent.education_agent.dto.SmsCodeRequest;
import com.qoobot.agent.education_agent.dto.StageSwitchRequest;
import com.qoobot.agent.education_agent.security.SecurityContextHolder;
import com.qoobot.agent.education_agent.service.UserService;
import com.qoobot.agent.education_agent.vo.LoginVO;
import com.qoobot.agent.education_agent.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证授权控制器
 */
@Tag(name = "认证授权", description = "用户注册/登录/Token/学段切换")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok("注册成功", userService.register(request));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok("登录成功", userService.login(request));
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@Parameter(description = "Refresh Token")
                                   @RequestHeader("Refresh-Token") String refreshToken) {
        return Result.ok("刷新成功", userService.refreshToken(refreshToken));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = SecurityContextHolder.requireUserId();
        userService.logout(userId);
        return Result.ok();
    }

    @Operation(summary = "获取当前用户档案")
    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        Long userId = SecurityContextHolder.requireUserId();
        return Result.ok(userService.getProfile(userId));
    }

    @Operation(summary = "切换学段")
    @PutMapping("/profile/stage")
    public Result<Void> switchStage(@Valid @RequestBody StageSwitchRequest request) {
        Long userId = SecurityContextHolder.requireUserId();
        userService.switchStage(userId, request);
        return Result.ok("学段已切换", null);
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms-code")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        userService.sendSmsCode(request.getPhone(), request.getBizType());
        return Result.ok("验证码已发送", null);
    }
}
