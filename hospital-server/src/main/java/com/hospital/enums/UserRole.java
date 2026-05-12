package com.hospital.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserRole {
    PATIENT(0, "患者"),
    DOCTOR(1, "医生"),
    ADMIN(2, "管理员");

    @EnumValue
    private final int code;
    private final String desc;

    UserRole(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
