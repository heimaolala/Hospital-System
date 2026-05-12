package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.common.BusinessException;
import com.hospital.common.PageResult;
import com.hospital.config.AutoAuditConfig;
import com.hospital.dto.request.ScheduleRequest;
import com.hospital.dto.response.ScheduleResponse;
import com.hospital.entity.DoctorProfile;
import com.hospital.entity.Schedule;
import com.hospital.entity.ScheduleAudit;
import com.hospital.entity.User;
import com.hospital.enums.ScheduleStatus;
import com.hospital.mapper.DoctorProfileMapper;
import com.hospital.mapper.ScheduleAuditMapper;
import com.hospital.mapper.ScheduleMapper;
import com.hospital.mapper.UserMapper;
import com.hospital.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final UserMapper userMapper;
    private final ScheduleAuditMapper scheduleAuditMapper;
    private final AutoAuditConfig autoAuditConfig;

    @Override
    @Transactional
    public ScheduleResponse createSchedule(Long doctorId, ScheduleRequest request) {
        DoctorProfile profile = doctorProfileMapper.selectOne(
                new LambdaQueryWrapper<DoctorProfile>().eq(DoctorProfile::getUserId, doctorId));
        if (profile == null || profile.getDepartment() == null) {
            throw new BusinessException("请先完善医生信息");
        }
        Schedule schedule = new Schedule();
        schedule.setDoctorId(doctorId);
        schedule.setDepartment(profile.getDepartment());
        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setTimeSlot(request.getTimeSlot());
        schedule.setTotalQuota(request.getTotalQuota());
        schedule.setRemainingQuota(request.getTotalQuota());
        schedule.setStatus(ScheduleStatus.PENDING.getCode());
        schedule.setIsRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : 0);
        schedule.setRecurPattern(request.getRecurPattern());
        scheduleMapper.insert(schedule);

        boolean isRecurring = request.getIsRecurring() != null && request.getIsRecurring() == 1
                && request.getRecurPattern() != null && !request.getRecurPattern().isEmpty();
        if (isRecurring) {
            generateRecurringSchedules(doctorId, profile.getDepartment(), request, schedule.getId());
        }
        if (!isRecurring && shouldAutoAudit(request)) {
            schedule.setStatus(ScheduleStatus.PUBLISHED.getCode());
            scheduleMapper.updateById(schedule);
        }

        return toResponse(schedule);
    }

    private boolean shouldAutoAudit(ScheduleRequest request) {
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), request.getScheduleDate());
        return daysUntil >= 0 && daysUntil <= autoAuditConfig.getMaxDays()
                && request.getTotalQuota() <= autoAuditConfig.getMaxQuota();
    }

    private void generateRecurringSchedules(Long doctorId, String department,
                                            ScheduleRequest request, Long masterId) {
        java.util.Set<Integer> targetDays = parseDayOfWeekPattern(request.getRecurPattern());
        if (targetDays.isEmpty()) return;
        LocalDate startDate = request.getScheduleDate();
        for (int week = 0; week <= 4; week++) {
            for (int dayOfWeek : targetDays) {
                LocalDate date = startDate.plusWeeks(week).with(
                        java.time.temporal.TemporalAdjusters.nextOrSame(
                                java.time.DayOfWeek.of(dayOfWeek)));
                if (!date.isAfter(startDate)) continue;
                if (date.isAfter(startDate.plusWeeks(4))) continue;
                Schedule s = new Schedule();
                s.setDoctorId(doctorId);
                s.setDepartment(department);
                s.setScheduleDate(date);
                s.setTimeSlot(request.getTimeSlot());
                s.setTotalQuota(request.getTotalQuota());
                s.setRemainingQuota(request.getTotalQuota());
                s.setStatus(ScheduleStatus.PENDING.getCode());
                s.setIsRecurring(1);
                s.setRecurPattern(request.getRecurPattern());
                scheduleMapper.insert(s);
            }
        }
    }

    private java.util.Set<Integer> parseDayOfWeekPattern(String pattern) {
        java.util.Map<String, Integer> dayMap = java.util.Map.of(
                "MON", 1, "TUE", 2, "WED", 3, "THU", 4,
                "FRI", 5, "SAT", 6, "SUN", 7);
        java.util.Set<Integer> result = new java.util.LinkedHashSet<>();
        for (String part : pattern.split(",")) {
            Integer day = dayMap.get(part.trim().toUpperCase());
            if (day != null) result.add(day);
        }
        return result;
    }

    @Override
    @Transactional
    public ScheduleResponse updateSchedule(Long doctorId, Long scheduleId, ScheduleRequest request) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null || !schedule.getDoctorId().equals(doctorId)) {
            throw new BusinessException("无权编辑该出诊信息");
        }
        if (schedule.getStatus() == ScheduleStatus.REJECTED.getCode()) {
            throw new BusinessException("已拒绝的排班不可编辑");
        }
        int bookedCount = schedule.getTotalQuota() - schedule.getRemainingQuota();
        if (request.getTotalQuota() < bookedCount) {
            throw new BusinessException("号源数量不能小于已预约数量(" + bookedCount + ")");
        }
        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setTimeSlot(request.getTimeSlot());
        schedule.setTotalQuota(request.getTotalQuota());
        schedule.setRemainingQuota(request.getTotalQuota() - bookedCount);
        if (schedule.getStatus() == ScheduleStatus.PUBLISHED.getCode()) {
            schedule.setStatus(ScheduleStatus.PENDING.getCode());
        }
        if (shouldAutoAudit(request)) {
            schedule.setStatus(ScheduleStatus.PUBLISHED.getCode());
        }
        scheduleMapper.updateById(schedule);
        return toResponse(schedule);
    }

    @Override
    public void deleteSchedule(Long doctorId, Long scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null || !schedule.getDoctorId().equals(doctorId)) {
            throw new BusinessException("无权删除该出诊信息");
        }
        scheduleMapper.deleteById(scheduleId);
    }

    @Override
    public PageResult<ScheduleResponse> getMySchedules(Long doctorId, LocalDate startDate, LocalDate endDate, int page, int size) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, doctorId)
                .orderByAsc(Schedule::getScheduleDate)
                .orderByAsc(Schedule::getTimeSlot);
        if (startDate != null && endDate != null) {
            wrapper.between(Schedule::getScheduleDate, startDate, endDate);
        }
        Page<Schedule> pageParam = new Page<>(page, size);
        Page<Schedule> result = scheduleMapper.selectPage(pageParam, wrapper);
        List<ScheduleResponse> records = result.getRecords().stream().map(this::toResponse).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), page, size, records);
    }

    @Override
    public List<ScheduleResponse> getAvailableSchedules(String department, LocalDate startDate, LocalDate endDate) {
        List<Schedule> schedules = scheduleMapper.findAvailable(startDate, endDate, department);
        return schedules.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public PageResult<ScheduleResponse> getPendingSchedules(int page, int size) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getStatus, ScheduleStatus.PENDING.getCode())
                .orderByAsc(Schedule::getScheduleDate);
        Page<Schedule> pageParam = new Page<>(page, size);
        Page<Schedule> result = scheduleMapper.selectPage(pageParam, wrapper);
        List<ScheduleResponse> records = result.getRecords().stream().map(this::toResponse).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), page, size, records);
    }

    @Override
    @Transactional
    public void auditSchedule(Long scheduleId, Integer action, String comment, Long adminId) {
        if (action != 1 && action != 2) {
            throw new BusinessException("无效的审核操作");
        }
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("出诊信息不存在");
        }
        if (schedule.getStatus() != ScheduleStatus.PENDING.getCode()) {
            throw new BusinessException("该出诊信息不在待审核状态");
        }
        if (action == 1) {
            schedule.setStatus(ScheduleStatus.PUBLISHED.getCode());
        } else {
            schedule.setStatus(ScheduleStatus.REJECTED.getCode());
        }
        int affected = scheduleMapper.updateById(schedule);
        if (affected == 0) {
            throw new BusinessException("审核失败，请重试");
        }

        ScheduleAudit audit = new ScheduleAudit();
        audit.setScheduleId(scheduleId);
        audit.setAdminId(adminId);
        audit.setAction(action);
        audit.setComment(comment);
        scheduleAuditMapper.insert(audit);
    }

    private ScheduleResponse toResponse(Schedule s) {
        User doctor = userMapper.selectById(s.getDoctorId());
        String doctorName = doctor != null ? doctor.getName() : "未知";
        DoctorProfile profile = doctorProfileMapper.selectOne(
                new LambdaQueryWrapper<DoctorProfile>().eq(DoctorProfile::getUserId, s.getDoctorId()));
        return ScheduleResponse.builder()
                .id(s.getId())
                .doctorId(s.getDoctorId())
                .doctorName(doctorName)
                .department(s.getDepartment())
                .hospital(profile != null ? profile.getHospital() : null)
                .title(profile != null ? profile.getTitle() : null)
                .scheduleDate(s.getScheduleDate())
                .timeSlot(s.getTimeSlot())
                .timeSlotDesc(s.getTimeSlot() == 0 ? "上午" : "下午")
                .totalQuota(s.getTotalQuota())
                .remainingQuota(s.getRemainingQuota())
                .status(s.getStatus())
                .statusDesc(getStatusDesc(s.getStatus()))
                .createdAt(s.getCreatedAt())
                .build();
    }

    private String getStatusDesc(Integer status) {
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已发布";
            case 2 -> "已拒绝";
            default -> "未知";
        };
    }
}
