package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("registrations")
public class Registration {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;
    private Long scheduleId;
    private Long doctorId;
    private String department;
    private LocalDate registrationDate;
    private Integer timeSlot;
    private Integer status;
    private Integer paymentStatus;
    private LocalDateTime cancelTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
