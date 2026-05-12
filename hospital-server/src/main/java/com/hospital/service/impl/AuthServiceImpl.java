package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.BusinessException;
import com.hospital.config.AutoAuditConfig;
import com.hospital.dto.request.ChangePasswordRequest;
import com.hospital.dto.request.LoginRequest;
import com.hospital.dto.request.RegisterRequest;
import com.hospital.dto.response.LoginResponse;
import com.hospital.entity.DoctorProfile;
import com.hospital.entity.User;
import com.hospital.mapper.DoctorProfileMapper;
import com.hospital.mapper.UserMapper;
import com.hospital.security.JwtAuthenticationFilter;
import com.hospital.security.JwtTokenProvider;
import com.hospital.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AutoAuditConfig autoAuditConfig;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getIdCard, request.getIdCard()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账户尚未通过审批，请联系管理员");
        }
        if (user.getStatus() == 2) {
            throw new BusinessException("账户已被拒绝，请联系管理员");
        }
        if (!request.getPassword().equals(user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        user.setLastLogin(LocalDateTime.now());
        userMapper.updateById(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getIdCard(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getIdCard());

        return LoginResponse.of(accessToken, refreshToken, user.getId(), user.getName(),
                user.getRole(), user.getMustChangePwd() == 1);
    }

    @Override
    @Transactional
    public boolean register(RegisterRequest request) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getIdCard, request.getIdCard()));
        if (existing != null) {
            throw new BusinessException("该身份证号已注册");
        }
        if (request.getRole() == 2) {
            throw new BusinessException("不允许注册管理员账户");
        }
        User user = new User();
        user.setIdCard(request.getIdCard());
        user.setPasswordHash(request.getPassword());
        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        boolean autoApprove = (request.getRole() == 0 && autoAuditConfig.isAutoApprovePatient())
                || (request.getRole() == 1 && autoAuditConfig.isAutoApproveDoctor());
        user.setStatus(autoApprove ? 1 : 0);
        user.setMustChangePwd(0);
        userMapper.insert(user);

        if (request.getRole() == 1) {
            DoctorProfile profile = new DoctorProfile();
            profile.setUserId(user.getId());
            profile.setHospital(request.getHospital());
            profile.setDepartment(request.getDepartment());
            profile.setTitle(request.getTitle());
            profile.setSpecialty(request.getSpecialty());
            doctorProfileMapper.insert(profile);
        }
        return autoApprove;
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!request.getOldPassword().equals(user.getPasswordHash())) {
            throw new BusinessException("旧密码不正确");
        }
        user.setPasswordHash(request.getNewPassword());
        user.setMustChangePwd(0);
        userMapper.updateById(user);
    }

    @Override
    public void forceChangePassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getMustChangePwd() != 1) {
            throw new BusinessException("无需强制修改密码");
        }
        user.setPasswordHash(newPassword);
        user.setMustChangePwd(0);
        userMapper.updateById(user);
    }

    @Override
    public void logout(String tokenId) {
        JwtAuthenticationFilter.blacklistToken(tokenId);
    }
}
