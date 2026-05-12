package com.hospital.enums;

import lombok.Getter;

@Getter
public enum ScheduleStatus {
    PENDING(0, "待审核"),
    PUBLISHED(1, "已发布"),
    REJECTED(2, "已拒绝");

    private final int code;
    private final String desc;

    ScheduleStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
