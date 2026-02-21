package com.balmaya.repo;

import com.balmaya.domain.ScheduleItem;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ScheduleItemRepository extends VersionedRepository<ScheduleItem, UUID> {
  @Query("select si from ScheduleItem si where si.scheduleId = :scheduleId and si.current = true")
  List<ScheduleItem> findCurrentByScheduleId(@Param("scheduleId") UUID scheduleId);
}

