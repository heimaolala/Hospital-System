package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.response.UserInfoResponse;
import com.hospital.security.SecurityUtils;
import com.hospital.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser() {
        return Result.success(userService.getCurrentUser(SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/me")
    public Result<UserInfoResponse> updateCurrentUser(@RequestBody UserInfoResponse request) {
        return Result.success(userService.updateCurrentUser(SecurityUtils.getCurrentUserId(), request));
    }
}
