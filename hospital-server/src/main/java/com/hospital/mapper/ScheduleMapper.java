package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    @Select("SELECT * FROM schedules WHERE schedule_date = #{date} AND status = 1")
    List<Schedule> findPublishedByDate(@Param("date") LocalDate date);

    @Select("SELECT * FROM schedules WHERE schedule_date BETWEEN #{startDate} AND #{endDate} AND status = 1 AND department = #{department}")
    List<Schedule> findAvailable(@Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 @Param("department") String department);

    @Select("SELECT * FROM schedules WHERE doctor_id = #{doctorId} AND schedule_date BETWEEN #{startDate} AND #{endDate} ORDER BY schedule_date, time_slot")
    List<Schedule> findByDoctorAndDateRange(@Param("doctorId") Long doctorId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Update("UPDATE schedules SET remaining_quota = remaining_quota - 1, version = version + 1 WHERE id = #{id} AND remaining_quota > 0 AND version = #{version}")
    int decrementQuota(@Param("id") Long id, @Param("version") Integer version);

    @Update("UPDATE schedules SET remaining_quota = remaining_quota + 1 WHERE id = #{id}")
    int incrementQuota(@Param("id") Long id);
}
