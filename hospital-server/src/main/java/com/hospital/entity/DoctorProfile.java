package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("doctor_profiles")
public class DoctorProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String hospital;
    private String department;
    private String title;
    private String specialty;
}
