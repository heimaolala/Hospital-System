package com.hospital.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String department;
    private String hospital;
    private String title;
    private LocalDate scheduleDate;
    private Integer timeSlot;
    private String timeSlotDesc;
    private Integer totalQuota;
    private Integer remainingQuota;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createdAt;
}
