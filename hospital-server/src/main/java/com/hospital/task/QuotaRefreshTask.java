package com.hospital.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.entity.Schedule;
import com.hospital.enums.ScheduleStatus;
import com.hospital.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuotaRefreshTask {

    private final ScheduleMapper scheduleMapper;

    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshDailyQuota() {
        log.info("开始每日号源维护...");
        LocalDate today = LocalDate.now();

        // 自动完成过期的已发布号源
        List<Schedule> expiredSchedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getStatus, ScheduleStatus.PUBLISHED.getCode())
                        .lt(Schedule::getScheduleDate, today));
        for (Schedule s : expiredSchedules) {
            log.info("号源 {} 已过期，自动标记完成", s.getId());
        }

        // 滚动生成周期性号源
        List<Schedule> recurringSchedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getIsRecurring, 1)
                        .eq(Schedule::getStatus, ScheduleStatus.PUBLISHED.getCode()));
        Map<String, List<Schedule>> grouped = recurringSchedules.stream()
                .collect(Collectors.groupingBy(s -> s.getDoctorId() + ":" + s.getTimeSlot() + ":" + s.getRecurPattern()));

        for (List<Schedule> group : grouped.values()) {
            Schedule latest = group.stream()
                    .max((a, b) -> a.getScheduleDate().compareTo(b.getScheduleDate()))
                    .orElse(null);
            if (latest == null || latest.getScheduleDate().isAfter(today.plusDays(28))) continue;

            Schedule template = group.get(0);
            Set<Integer> targetDays = parseDayOfWeekPattern(template.getRecurPattern());
            LocalDate startFrom = latest.getScheduleDate().plusDays(1);
            for (int i = 0; i < 35; i++) {
                LocalDate date = startFrom.plusDays(i);
                if (date.isAfter(today.plusDays(35))) break;
                if (targetDays.contains(date.getDayOfWeek().getValue())) {
                    boolean exists = group.stream().anyMatch(s -> s.getScheduleDate().equals(date));
                    if (!exists) {
                        Schedule s = new Schedule();
                        s.setDoctorId(template.getDoctorId());
                        s.setDepartment(template.getDepartment());
                        s.setScheduleDate(date);
                        s.setTimeSlot(template.getTimeSlot());
                        s.setTotalQuota(template.getTotalQuota());
                        s.setRemainingQuota(template.getTotalQuota());
                        s.setStatus(ScheduleStatus.PENDING.getCode());
                        s.setIsRecurring(1);
                        s.setRecurPattern(template.getRecurPattern());
                        scheduleMapper.insert(s);
                        log.info("自动生成周期性号源: 医生={}, 日期={}", template.getDoctorId(), date);
                    }
                }
            }
        }
        log.info("每日号源维护完成");
    }

    private Set<Integer> parseDayOfWeekPattern(String pattern) {
        Map<String, Integer> dayMap = Map.of(
                "MON", 1, "TUE", 2, "WED", 3, "THU", 4,
                "FRI", 5, "SAT", 6, "SUN", 7);
        Set<Integer> result = new LinkedHashSet<>();
        for (String part : pattern.split(",")) {
            Integer day = dayMap.get(part.trim().toUpperCase());
            if (day != null) result.add(day);
        }
        return result;
    }
}
