package com.hospital.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistrationRequest {
    @NotNull(message = "出诊信息ID不能为空")
    private Long scheduleId;
}
