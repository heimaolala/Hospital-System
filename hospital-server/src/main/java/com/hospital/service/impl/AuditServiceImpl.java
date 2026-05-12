package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.common.PageResult;
import com.hospital.entity.AuditLog;
import com.hospital.mapper.AuditLogMapper;
import com.hospital.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogMapper auditLogMapper;

    @Override
    public void logOperation(Long operatorId, String targetType, Long targetId, String action, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        log.setOperatorId(operatorId);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        auditLogMapper.insert(log);
    }

    @Override
    public PageResult<AuditLog> getAuditLogs(int page, int size) {
        Page<AuditLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<AuditLog>()
                .orderByDesc(AuditLog::getCreatedAt);
        Page<AuditLog> result = auditLogMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getTotal(), page, size, result.getRecords());
    }
}
