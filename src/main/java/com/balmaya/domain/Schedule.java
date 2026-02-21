package com.balmaya.domain;

import com.balmaya.domain.Enums.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name="schedules",
  uniqueConstraints = {
    @UniqueConstraint(name="uq_schedule_logical_version", columnNames={"logical_id","version"})
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Schedule extends VersionedEntity {
  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Column(name="user_id", nullable=false)
  private String userId;

  @Column(name="beneficiary_id", nullable=false)
  private UUID beneficiaryId;

  @Enumerated(EnumType.STRING)
  @Column(name="status", nullable=false)
  private ScheduleStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name="frequency", nullable=false)
  private Frequency frequency;

  @Column(name="timezone", nullable=false)
  private String timezone;

  @Column(name="start_at", nullable=false)
  private OffsetDateTime startAt;

  @Column(name="weekly_day")
  private Integer weeklyDay;

  @Column(name="monthly_day")
  private Integer monthlyDay;

  @Column(name="next_run_at", nullable=false)
  private OffsetDateTime nextRunAt;

  @Column(name="net_amount", nullable=false)
  private long netAmount;

  @Column(name="currency", nullable=false)
  private String currency;

  @Column(name="funding_payment_method_ref")
  private String fundingPaymentMethodRef;
}

