package com.hospital.service;

import com.hospital.common.PageResult;
import com.hospital.entity.AuditLog;

public interface AuditService {
    void logOperation(Long operatorId, String targetType, Long targetId, String action, String oldValue, String newValue);
    PageResult<AuditLog> getAuditLogs(int page, int size);
}
