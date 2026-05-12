package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.common.BusinessException;
import com.hospital.dto.response.PatientRecordResponse;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.User;
import com.hospital.mapper.MedicalRecordMapper;
import com.hospital.mapper.UserMapper;
import com.hospital.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordMapper medicalRecordMapper;
    private final UserMapper userMapper;

    @Override
    public List<MedicalRecord> getByPatientId(Long patientId) {
        return medicalRecordMapper.selectList(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getPatientId, patientId)
                        .orderByDesc(MedicalRecord::getCreatedAt));
    }

    @Override
    public List<MedicalRecord> getMyRecords(Long patientId) {
        return getByPatientId(patientId);
    }

    @Override
    public List<PatientRecordResponse> searchPatients(String name, String idCard) {
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            userWrapper.like(User::getName, name);
        }
        if (idCard != null && !idCard.isEmpty()) {
            userWrapper.eq(User::getIdCard, idCard);
        }
        if ((name == null || name.isEmpty()) && (idCard == null || idCard.isEmpty())) {
            throw new BusinessException("请输入患者姓名或身份证号");
        }
        userWrapper.eq(User::getRole, 0);
        List<User> patients = userMapper.selectList(userWrapper);

        List<PatientRecordResponse> result = new ArrayList<>();
        for (User patient : patients) {
            PatientRecordResponse resp = new PatientRecordResponse();
            resp.setPatientId(patient.getId());
            resp.setName(patient.getName());
            resp.setIdCard(patient.getIdCard());
            resp.setRecords(getByPatientId(patient.getId()));
            result.add(resp);
        }
        return result;
    }

    @Override
    public MedicalRecord create(Long doctorId, Long patientId, String content) {
        MedicalRecord record = new MedicalRecord();
        record.setPatientId(patientId);
        record.setContent(content);
        medicalRecordMapper.insert(record);
        return record;
    }

    @Override
    public MedicalRecord update(Long id, Long doctorId, String content) {
        MedicalRecord record = medicalRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("病历记录不存在");
        }
        record.setContent(content);
        medicalRecordMapper.updateById(record);
        return record;
    }
}
