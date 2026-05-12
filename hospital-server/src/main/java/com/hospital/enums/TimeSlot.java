package com.hospital.enums;

import lombok.Getter;

@Getter
public enum TimeSlot {
    AM(0, "上午"),
    PM(1, "下午");

    private final int code;
    private final String desc;

    TimeSlot(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
