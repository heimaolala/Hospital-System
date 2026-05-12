package com.hospital.controller;

import com.hospital.common.BusinessException;
import com.hospital.common.Result;
import com.hospital.dto.response.PatientRecordResponse;
import com.hospital.entity.MedicalRecord;
import com.hospital.security.SecurityUtils;
import com.hospital.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping("/my")
    public Result<List<MedicalRecord>> getMyRecords() {
        return Result.success(medicalRecordService.getMyRecords(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/patient/{patientId}")
    public Result<List<MedicalRecord>> getByPatientId(@PathVariable Long patientId) {
        return Result.success(medicalRecordService.getByPatientId(patientId));
    }

    @GetMapping("/search")
    public Result<List<PatientRecordResponse>> searchPatients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String idCard) {
        return Result.success(medicalRecordService.searchPatients(name, idCard));
    }

    @PostMapping
    public Result<MedicalRecord> create(@RequestBody Map<String, Object> body) {
        Long patientId = body.get("patientId") != null
                ? ((Number) body.get("patientId")).longValue() : null;
        String content = (String) body.get("content");
        if (patientId == null || content == null || content.isEmpty()) {
            throw new BusinessException("患者ID和内容不能为空");
        }
        return Result.success(medicalRecordService.create(
                SecurityUtils.getCurrentUserId(), patientId, content));
    }

    @PutMapping("/{id}")
    public Result<MedicalRecord> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.isEmpty()) {
            throw new BusinessException("内容不能为空");
        }
        return Result.success(medicalRecordService.update(id, SecurityUtils.getCurrentUserId(), content));
    }
}
