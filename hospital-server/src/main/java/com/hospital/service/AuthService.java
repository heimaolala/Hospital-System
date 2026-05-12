package com.hospital.service;

import com.hospital.dto.request.ChangePasswordRequest;
import com.hospital.dto.request.LoginRequest;
import com.hospital.dto.request.RegisterRequest;
import com.hospital.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    boolean register(RegisterRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    void forceChangePassword(Long userId, String newPassword);
    void logout(String tokenId);
}
