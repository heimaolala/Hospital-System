package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.BusinessException;
import com.hospital.dto.response.UserInfoResponse;
import com.hospital.entity.DoctorProfile;
import com.hospital.entity.User;
import com.hospital.mapper.DoctorProfileMapper;
import com.hospital.mapper.UserMapper;
import com.hospital.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final DoctorProfileMapper doctorProfileMapper;

    @Override
    public UserInfoResponse getCurrentUser(Long userId) {
        return getUserById(userId);
    }

    @Override
    @Transactional
    public UserInfoResponse updateCurrentUser(Long userId, UserInfoResponse request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (request.getName() != null) user.setName(request.getName());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        userMapper.updateById(user);

        if (user.getRole() == 1) {
            DoctorProfile profile = doctorProfileMapper.selectOne(
                    new LambdaQueryWrapper<DoctorProfile>().eq(DoctorProfile::getUserId, userId));
            if (profile != null) {
                if (request.getHospital() != null) profile.setHospital(request.getHospital());
                if (request.getDepartment() != null) profile.setDepartment(request.getDepartment());
                if (request.getTitle() != null) profile.setTitle(request.getTitle());
                if (request.getSpecialty() != null) profile.setSpecialty(request.getSpecialty());
                doctorProfileMapper.updateById(profile);
            }
        }
        return getUserById(userId);
    }

    @Override
    public UserInfoResponse getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserInfoResponse response = UserInfoResponse.builder()
                .id(user.getId())
                .idCard(user.getIdCard())
                .name(user.getName())
                .age(user.getAge())
                .gender(user.getGender())
                .address(user.getAddress())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
        if (user.getRole() == 1) {
            DoctorProfile profile = doctorProfileMapper.selectOne(
                    new LambdaQueryWrapper<DoctorProfile>().eq(DoctorProfile::getUserId, userId));
            if (profile != null) {
                response.setHospital(profile.getHospital());
                response.setDepartment(profile.getDepartment());
                response.setTitle(profile.getTitle());
                response.setSpecialty(profile.getSpecialty());
            }
        }
        return response;
    }
}
