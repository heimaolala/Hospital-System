package com.hospital.service;

import com.hospital.dto.response.UserInfoResponse;

public interface UserService {
    UserInfoResponse getCurrentUser(Long userId);
    UserInfoResponse updateCurrentUser(Long userId, UserInfoResponse request);
    UserInfoResponse getUserById(Long userId);
}
