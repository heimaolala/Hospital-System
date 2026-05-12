package com.hospital.service;

import com.hospital.common.PageResult;
import com.hospital.dto.request.ScheduleRequest;
import com.hospital.dto.response.ScheduleResponse;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    ScheduleResponse createSchedule(Long doctorId, ScheduleRequest request);
    ScheduleResponse updateSchedule(Long doctorId, Long scheduleId, ScheduleRequest request);
    void deleteSchedule(Long doctorId, Long scheduleId);
    PageResult<ScheduleResponse> getMySchedules(Long doctorId, LocalDate startDate, LocalDate endDate, int page, int size);
    List<ScheduleResponse> getAvailableSchedules(String department, LocalDate startDate, LocalDate endDate);
    PageResult<ScheduleResponse> getPendingSchedules(int page, int size);
    void auditSchedule(Long scheduleId, Integer action, String comment, Long adminId);
}
