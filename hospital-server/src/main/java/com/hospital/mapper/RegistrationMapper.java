package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    @Select("SELECT * FROM registrations WHERE patient_id = #{patientId} AND registration_date BETWEEN #{startDate} AND #{endDate} ORDER BY created_at DESC")
    List<Registration> findByPatientAndDateRange(@Param("patientId") Long patientId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Select("SELECT COUNT(*) FROM registrations WHERE patient_id = #{patientId} AND schedule_id = #{scheduleId} AND status = 0")
    int countActiveByPatientAndSchedule(@Param("patientId") Long patientId,
                                        @Param("scheduleId") Long scheduleId);

    @org.apache.ibatis.annotations.Update("UPDATE registrations SET status = 1, cancel_time = NOW() WHERE id = #{id} AND status = 0")
    int cancelById(@Param("id") Long id);

    @org.apache.ibatis.annotations.Update("UPDATE registrations SET payment_status = 1 WHERE id = #{id} AND payment_status = 0")
    int payById(@Param("id") Long id);
}
