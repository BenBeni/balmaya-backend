package com.balmaya.domain;

public final class Enums {
  private Enums() {}

  public enum Provider { WAVE, ORANGE_MONEY }

  public enum BeneficiaryStatus { ACTIVE, INACTIVE }

  public enum ScheduleStatus { ACTIVE, PAUSED, CANCELLED }

  public enum Frequency { WEEKLY, MONTHLY }

  public enum PaymentKind { ONE_SHOT, SCHEDULED_RUN }

  public enum PaymentStatus { CREATED, AWAITING_FUNDS, FUNDED, PROCESSING, COMPLETED, PARTIAL, FAILED, CANCELLED }

  public enum ItemCategory { CASH, FOOD, RENT, UTILITIES, ELECTRICITY, WATER, SCHOOL, OTHER }

  public enum ItemMethod { WALLET_PAYOUT, BILL_PAY }

  public enum ItemStatus { CREATED, PROCESSING, SUCCEEDED, FAILED }

  public enum FeeCode { SERVICE_FEE, PROCESSOR_FEE, FX_MARGIN, NETWORK_FEE, DISPUTE_FEE, OTHER }

  public enum FeeDirection { CHARGE, COST, REVENUE }

  public enum IntentStatus { REQUIRES_ACTION, SUCCEEDED, FAILED }

  public enum ExecutionStatus { CREATED, SENT, SUCCEEDED, FAILED }
}

