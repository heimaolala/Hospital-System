package com.hospital.service;

import com.hospital.dto.request.RegistrationRequest;
import com.hospital.dto.response.RegistrationResponse;

import java.time.LocalDate;
import java.util.List;

public interface RegistrationService {
    RegistrationResponse register(Long patientId, RegistrationRequest request);
    void pay(Long registrationId, Long patientId);
    void cancel(Long registrationId, Long patientId);
    List<RegistrationResponse> getMyRegistrations(Long patientId, LocalDate startDate, LocalDate endDate);
    Integer getRemainingQuota(Long scheduleId);
}
