package com.hospital.controller;

import com.hospital.common.PageResult;
import com.hospital.common.Result;
import com.hospital.dto.request.ScheduleRequest;
import com.hospital.dto.response.ScheduleResponse;
import com.hospital.security.SecurityUtils;
import com.hospital.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public Result<ScheduleResponse> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        return Result.success(scheduleService.createSchedule(SecurityUtils.getCurrentUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<ScheduleResponse> updateSchedule(@PathVariable Long id, @Valid @RequestBody ScheduleRequest request) {
        return Result.success(scheduleService.updateSchedule(SecurityUtils.getCurrentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(SecurityUtils.getCurrentUserId(), id);
        return Result.success("删除成功");
    }

    @GetMapping("/my")
    public Result<PageResult<ScheduleResponse>> getMySchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(scheduleService.getMySchedules(SecurityUtils.getCurrentUserId(), startDate, endDate, page, size));
    }

    @GetMapping("/available")
    public Result<List<ScheduleResponse>> getAvailableSchedules(
            @RequestParam String department,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now();
            endDate = startDate.plusDays(2);
        }
        if (endDate == null) endDate = startDate;
        return Result.success(scheduleService.getAvailableSchedules(department, startDate, endDate));
    }
}
