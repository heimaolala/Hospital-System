package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.dto.request.RegistrationRequest;
import com.hospital.dto.response.RegistrationResponse;
import com.hospital.security.SecurityUtils;
import com.hospital.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public Result<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        return Result.success("挂号成功，请支付",
                registrationService.register(SecurityUtils.getCurrentUserId(), request));
    }

    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        registrationService.pay(id, SecurityUtils.getCurrentUserId());
        return Result.success("支付成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        registrationService.cancel(id, SecurityUtils.getCurrentUserId());
        return Result.success("挂号已取消");
    }

    @GetMapping("/my")
    public Result<List<RegistrationResponse>> getMyRegistrations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now().plusMonths(1);
        return Result.success(registrationService.getMyRegistrations(SecurityUtils.getCurrentUserId(), startDate, endDate));
    }

    @GetMapping("/quota/{scheduleId}")
    public Result<Integer> getRemainingQuota(@PathVariable Long scheduleId) {
        return Result.success(registrationService.getRemainingQuota(scheduleId));
    }
}
