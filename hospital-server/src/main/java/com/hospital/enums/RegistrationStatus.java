package com.hospital.enums;

import lombok.Getter;

@Getter
public enum RegistrationStatus {
    BOOKED(0, "已预约"),
    CANCELLED(1, "已取消"),
    COMPLETED(2, "已完成");

    private final int code;
    private final String desc;

    RegistrationStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
