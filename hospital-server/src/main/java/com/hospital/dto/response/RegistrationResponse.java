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
public class RegistrationResponse {
    private Long id;
    private Long patientId;
    private Long scheduleId;
    private Long doctorId;
    private String doctorName;
    private String department;
    private String hospital;
    private LocalDate registrationDate;
    private Integer timeSlot;
    private String timeSlotDesc;
    private Integer status;
    private String statusDesc;
    private Integer paymentStatus;
    private String paymentStatusDesc;
    private LocalDateTime cancelTime;
    private LocalDateTime createdAt;
}
