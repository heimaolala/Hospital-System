package com.hospital.dto.response;

import com.hospital.entity.MedicalRecord;
import lombok.Data;
import java.util.List;

@Data
public class PatientRecordResponse {
    private Long patientId;
    private String name;
    private String idCard;
    private List<MedicalRecord> records;
}
