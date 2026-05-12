package com.hospital.service;

import com.hospital.dto.response.PatientRecordResponse;
import com.hospital.entity.MedicalRecord;

import java.util.List;

public interface MedicalRecordService {
    List<MedicalRecord> getByPatientId(Long patientId);
    List<MedicalRecord> getMyRecords(Long patientId);
    List<PatientRecordResponse> searchPatients(String name, String idCard);
    MedicalRecord create(Long doctorId, Long patientId, String content);
    MedicalRecord update(Long id, Long doctorId, String content);
}
