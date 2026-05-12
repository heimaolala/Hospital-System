package com.hospital.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ScheduleRequest {
    @NotNull(message = "出诊日期不能为空")
    @FutureOrPresent(message = "出诊日期不能是过去日期")
    private LocalDate scheduleDate;

    @NotNull(message = "时间段不能为空")
    private Integer timeSlot;

    @NotNull(message = "号源数量不能为空")
    @Min(value = 1, message = "号源数量至少为1")
    private Integer totalQuota;

    private Integer isRecurring;
    private String recurPattern;
}
