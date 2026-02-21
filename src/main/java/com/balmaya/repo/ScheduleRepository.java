package com.balmaya.repo;

import com.balmaya.domain.Schedule;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends VersionedRepository<Schedule, UUID> {
  @Query("select s from Schedule s where s.userId = :userId and s.current = true order by s.createdAt desc")
  List<Schedule> findCurrentByUser(@Param("userId") String userId);

  @Query("select s from Schedule s where s.current = true and s.status = com.balmaya.domain.Enums$ScheduleStatus.ACTIVE and s.nextRunAt <= :now")
  List<Schedule> findDueSchedules(@Param("now") OffsetDateTime now);
}

