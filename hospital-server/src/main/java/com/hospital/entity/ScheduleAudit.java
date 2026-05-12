package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("schedule_audits")
public class ScheduleAudit {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scheduleId;
    private Long adminId;
    private Integer action;
    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
