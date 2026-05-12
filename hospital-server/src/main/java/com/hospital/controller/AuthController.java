package com.hospital.controller;

import com.hospital.common.BusinessException;
import com.hospital.common.Result;
import com.hospital.dto.request.ChangePasswordRequest;
import com.hospital.dto.request.LoginRequest;
import com.hospital.dto.request.RegisterRequest;
import com.hospital.dto.response.LoginResponse;
import com.hospital.security.JwtTokenProvider;
import com.hospital.security.SecurityUtils;
import com.hospital.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        boolean autoApproved = authService.register(request);
        return Result.success(autoApproved ? "注册成功，已自动通过审批" : "注册成功，请等待管理员审批");
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("无效的认证头");
        }
        String token = authHeader.substring(7);
        String tokenId = jwtTokenProvider.getTokenId(token);
        authService.logout(tokenId);
        return Result.success("已退出登录");
    }

    @PutMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(SecurityUtils.getCurrentUserId(), request);
        return Result.success("密码修改成功");
    }

    @PostMapping("/force-change-password")
    public Result<Void> forceChangePassword(@RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException("密码长度为6-20位");
        }
        authService.forceChangePassword(SecurityUtils.getCurrentUserId(), newPassword);
        return Result.success("密码修改成功");
    }
}
