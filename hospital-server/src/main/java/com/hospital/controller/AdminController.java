package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.common.BusinessException;
import com.hospital.common.PageResult;
import com.hospital.common.Result;
import com.hospital.config.AutoAuditConfig;
import com.hospital.dto.response.ScheduleResponse;
import com.hospital.dto.response.UserInfoResponse;
import com.hospital.entity.AuditLog;
import com.hospital.entity.DoctorProfile;
import com.hospital.entity.User;
import com.hospital.mapper.DoctorProfileMapper;
import com.hospital.mapper.UserMapper;
import com.hospital.security.SecurityUtils;
import com.hospital.service.AuditService;
import com.hospital.service.ScheduleService;
import com.hospital.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final UserService userService;
    private final AuditService auditService;
    private final ScheduleService scheduleService;
    private final ObjectMapper objectMapper;
    private final AutoAuditConfig autoAuditConfig;

    @GetMapping("/users")
    public Result<PageResult<User>> getUsers(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) Integer role,
                                              @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByAsc(User::getId);
        Page<User> pageParam = new Page<>(page, size);
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        return Result.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @PutMapping("/users/{id}")
    @Transactional
    @SneakyThrows
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody UserInfoResponse request) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        String oldValue = objectMapper.writeValueAsString(userService.getUserById(id));
        if (request.getName() != null) user.setName(request.getName());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        userMapper.updateById(user);

        if (user.getRole() == 1) {
            DoctorProfile profile = doctorProfileMapper.selectOne(
                    new LambdaQueryWrapper<DoctorProfile>().eq(DoctorProfile::getUserId, id));
            if (profile != null) {
                if (request.getHospital() != null) profile.setHospital(request.getHospital());
                if (request.getDepartment() != null) profile.setDepartment(request.getDepartment());
                if (request.getTitle() != null) profile.setTitle(request.getTitle());
                if (request.getSpecialty() != null) profile.setSpecialty(request.getSpecialty());
                doctorProfileMapper.updateById(profile);
            }
        }

        String newValue = objectMapper.writeValueAsString(userService.getUserById(id));
        auditService.logOperation(SecurityUtils.getCurrentUserId(), "user", id, "update", oldValue, newValue);
        return Result.success("修改成功");
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    @SneakyThrows
    public Result<Void> deleteUser(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        String oldValue = objectMapper.writeValueAsString(userService.getUserById(id));
        if (user.getRole() == 1) {
            DoctorProfile profile = doctorProfileMapper.selectOne(
                    new LambdaQueryWrapper<DoctorProfile>().eq(DoctorProfile::getUserId, id));
            if (profile != null) {
                doctorProfileMapper.deleteById(profile.getId());
            }
        }
        userMapper.deleteById(id);
        auditService.logOperation(SecurityUtils.getCurrentUserId(), "user", id, "delete", oldValue, null);
        return Result.success("删除成功");
    }

    @PostMapping("/users/{id}/approve")
    @Transactional
    @SneakyThrows
    public Result<Void> approveUser(@PathVariable Long id, @RequestParam Integer action) {
        if (action != 1 && action != 2) {
            throw new BusinessException("无效的审批操作");
        }
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        if (user.getStatus() != 0) throw new BusinessException("该用户不在待审批状态");
        String oldValue = objectMapper.writeValueAsString(userService.getUserById(id));
        user.setStatus(action);
        userMapper.updateById(user);
        String newValue = objectMapper.writeValueAsString(userService.getUserById(id));
        String actionName = action == 1 ? "approve" : "reject";
        auditService.logOperation(SecurityUtils.getCurrentUserId(), "user", id, actionName, oldValue, newValue);
        return Result.success(action == 1 ? "审批通过" : "已拒绝");
    }

    @GetMapping("/audit-logs")
    public Result<PageResult<AuditLog>> getAuditLogs(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return Result.success(auditService.getAuditLogs(page, size));
    }

    @GetMapping("/schedules/pending")
    public Result<PageResult<ScheduleResponse>> getPendingSchedules(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(scheduleService.getPendingSchedules(page, size));
    }

    @PostMapping("/schedules/{id}/audit")
    public Result<Void> auditSchedule(@PathVariable Long id,
                                       @RequestParam Integer action,
                                       @RequestParam(required = false) String comment) {
        scheduleService.auditSchedule(id, action, comment, SecurityUtils.getCurrentUserId());
        return Result.success(action == 1 ? "审核通过" : "已拒绝");
    }

    @GetMapping("/auto-audit-config")
    public Result<Map<String, Object>> getAutoAuditConfig() {
        return Result.success(Map.of(
            "maxDays", autoAuditConfig.getMaxDays(),
            "maxQuota", autoAuditConfig.getMaxQuota(),
            "autoApprovePatient", autoAuditConfig.isAutoApprovePatient(),
            "autoApproveDoctor", autoAuditConfig.isAutoApproveDoctor()
        ));
    }

    @PutMapping("/auto-audit-config")
    public Result<Void> updateAutoAuditConfig(@RequestBody Map<String, Object> body) {
        if (body.containsKey("maxDays")) autoAuditConfig.setMaxDays(((Number) body.get("maxDays")).intValue());
        if (body.containsKey("maxQuota")) autoAuditConfig.setMaxQuota(((Number) body.get("maxQuota")).intValue());
        if (body.containsKey("autoApprovePatient")) autoAuditConfig.setAutoApprovePatient((Boolean) body.get("autoApprovePatient"));
        if (body.containsKey("autoApproveDoctor")) autoAuditConfig.setAutoApproveDoctor((Boolean) body.get("autoApproveDoctor"));
        return Result.success("自动审核配置已更新");
    }

    @PostMapping("/users/batch-approve")
    @Transactional
    @SneakyThrows
    public Result<Void> batchApproveUsers(@RequestBody Map<String, Object> body) {
        List<Integer> idsRaw = (List<Integer>) body.get("ids");
        Integer action = ((Number) body.get("action")).intValue();
        if (action != 1 && action != 2) throw new BusinessException("无效的审批操作");
        if (idsRaw == null || idsRaw.isEmpty()) throw new BusinessException("请选择用户");
        Long operatorId = SecurityUtils.getCurrentUserId();
        int processed = 0;
        for (Integer id : idsRaw) {
            User user = userMapper.selectById(id.longValue());
            if (user == null || user.getStatus() != 0) continue;
            String oldValue = objectMapper.writeValueAsString(userService.getUserById(id.longValue()));
            user.setStatus(action);
            userMapper.updateById(user);
            String newValue = objectMapper.writeValueAsString(userService.getUserById(id.longValue()));
            String actionName = action == 1 ? "approve" : "reject";
            auditService.logOperation(operatorId, "user", id.longValue(), actionName, oldValue, newValue);
            processed++;
        }
        String label = action == 1 ? "通过" : "拒绝";
        return Result.success(String.format("批量%s完成，共处理 %d 条，跳过 %d 条非待审批用户",
                label, processed, idsRaw.size() - processed));
    }

    @PostMapping("/schedules/batch-audit")
    @Transactional
    public Result<Void> batchAuditSchedules(@RequestBody Map<String, Object> body) {
        List<Integer> idsRaw = (List<Integer>) body.get("ids");
        Integer action = ((Number) body.get("action")).intValue();
        String comment = (String) body.getOrDefault("comment", "");
        if (action != 1 && action != 2) throw new BusinessException("无效的审核操作");
        if (idsRaw == null || idsRaw.isEmpty()) throw new BusinessException("请选择排班");
        Long adminId = SecurityUtils.getCurrentUserId();
        for (Integer id : idsRaw) {
            scheduleService.auditSchedule(id.longValue(), action, comment, adminId);
        }
        return Result.success(action == 1 ? "批量审核通过" : "批量已拒绝");
    }
}
