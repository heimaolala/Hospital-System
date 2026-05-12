package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.BusinessException;
import com.hospital.dto.request.RegistrationRequest;
import com.hospital.dto.response.RegistrationResponse;
import com.hospital.entity.*;
import com.hospital.enums.RegistrationStatus;
import com.hospital.enums.ScheduleStatus;
import com.hospital.mapper.*;
import com.hospital.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final ScheduleMapper scheduleMapper;
    private final UserMapper userMapper;
    private final DoctorProfileMapper doctorProfileMapper;

    @Override
    @Transactional
    public RegistrationResponse register(Long patientId, RegistrationRequest request) {
        Schedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null) {
            throw new BusinessException(400, "该号源不存在或已失效，请刷新页面重试");
        }
        if (schedule.getStatus() != ScheduleStatus.PUBLISHED.getCode()) {
            throw new BusinessException(400, "该号源暂未开放或已被预约满");
        }
        LocalDate today = LocalDate.now();
        if (schedule.getScheduleDate().isBefore(today) || schedule.getScheduleDate().isAfter(today.plusDays(2))) {
            throw new BusinessException("只能预约3天内的号源");
        }

        // 当日预约时段限制
        if (schedule.getScheduleDate().isEqual(today)) {
            int currentHour = java.time.LocalTime.now().getHour();
            if (schedule.getTimeSlot() == 0 && (currentHour < 7 || currentHour >= 9)) {
                throw new BusinessException("上午号源请在7:00-9:00之间预约");
            }
            if (schedule.getTimeSlot() == 1 && currentHour >= 14) {
                throw new BusinessException("下午号源请在14:00之前预约");
            }
        }

        int count = registrationMapper.countActiveByPatientAndSchedule(patientId, request.getScheduleId());
        if (count > 0) {
            throw new BusinessException("请勿重复挂号");
        }

        // 清理同患者同号源的已取消记录，避免唯一键冲突
        registrationMapper.delete(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getPatientId, patientId)
                .eq(Registration::getScheduleId, request.getScheduleId())
                .eq(Registration::getStatus, RegistrationStatus.CANCELLED.getCode()));

        int affected = scheduleMapper.decrementQuota(request.getScheduleId(), schedule.getVersion());
        if (affected == 0) {
            throw new BusinessException("号源已满，请刷新后重试");
        }

        Registration registration = new Registration();
        registration.setPatientId(patientId);
        registration.setScheduleId(schedule.getId());
        registration.setDoctorId(schedule.getDoctorId());
        registration.setDepartment(schedule.getDepartment());
        registration.setRegistrationDate(schedule.getScheduleDate());
        registration.setTimeSlot(schedule.getTimeSlot());
        registration.setStatus(RegistrationStatus.BOOKED.getCode());
        registration.setPaymentStatus(0);
        registration.setCreatedAt(LocalDateTime.now());
        registrationMapper.insert(registration);

        return toResponse(registration);
    }

    @Override
    @Transactional
    public void pay(Long registrationId, Long patientId) {
        Registration registration = registrationMapper.selectById(registrationId);
        if (registration == null || !registration.getPatientId().equals(patientId)) {
            throw new BusinessException("挂号记录不存在");
        }
        if (registration.getStatus() == RegistrationStatus.CANCELLED.getCode()) {
            throw new BusinessException("挂号已取消");
        }
        if (registration.getPaymentStatus() == 1) {
            throw new BusinessException("已支付，无需重复支付");
        }
        int affected = registrationMapper.payById(registrationId);
        if (affected == 0) {
            throw new BusinessException("支付失败，请重试");
        }
    }

    @Override
    @Transactional
    public void cancel(Long registrationId, Long patientId) {
        Registration registration = registrationMapper.selectById(registrationId);
        if (registration == null || !registration.getPatientId().equals(patientId)) {
            throw new BusinessException("挂号记录不存在");
        }
        if (registration.getStatus() == RegistrationStatus.CANCELLED.getCode()) {
            throw new BusinessException("挂号已取消");
        }
        if (!LocalDate.now().isBefore(registration.getRegistrationDate())) {
            throw new BusinessException("只能在就诊日期前一天及之前取消挂号");
        }
        int affected = registrationMapper.cancelById(registrationId);
        if (affected == 0) {
            throw new BusinessException("取消失败，请重试");
        }
        scheduleMapper.incrementQuota(registration.getScheduleId());
    }

    @Override
    public List<RegistrationResponse> getMyRegistrations(Long patientId, LocalDate startDate, LocalDate endDate) {
        List<Registration> registrations = registrationMapper.findByPatientAndDateRange(patientId, startDate, endDate);
        return registrations.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Integer getRemainingQuota(Long scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        return schedule != null ? schedule.getRemainingQuota() : 0;
    }

    private RegistrationResponse toResponse(Registration r) {
        User doctor = userMapper.selectById(r.getDoctorId());
        DoctorProfile profile = doctorProfileMapper.selectOne(
                new LambdaQueryWrapper<DoctorProfile>().eq(DoctorProfile::getUserId, r.getDoctorId()));
        return RegistrationResponse.builder()
                .id(r.getId())
                .patientId(r.getPatientId())
                .scheduleId(r.getScheduleId())
                .doctorId(r.getDoctorId())
                .doctorName(doctor != null ? doctor.getName() : "未知")
                .department(r.getDepartment())
                .hospital(profile != null ? profile.getHospital() : null)
                .registrationDate(r.getRegistrationDate())
                .timeSlot(r.getTimeSlot())
                .timeSlotDesc(r.getTimeSlot() == 0 ? "上午" : "下午")
                .status(r.getStatus())
                .statusDesc(getStatusDesc(r.getStatus()))
                .paymentStatus(r.getPaymentStatus())
                .paymentStatusDesc(r.getPaymentStatus() == 1 ? "已支付" : "未支付")
                .cancelTime(r.getCancelTime())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private String getStatusDesc(Integer status) {
        return switch (status) {
            case 0 -> "已预约";
            case 1 -> "已取消";
            case 2 -> "已完成";
            default -> "未知";
        };
    }
}
