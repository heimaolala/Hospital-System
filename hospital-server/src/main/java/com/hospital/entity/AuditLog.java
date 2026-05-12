package com.hospital.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_logs")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long operatorId;
    private String targetType;
    private Long targetId;
    private String action;
    private String oldValue;
    private String newValue;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
