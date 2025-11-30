package com.example.capstonedesign20252.paymentCycle.dto;

import com.example.capstonedesign20252.paymentCycle.domain.PaymentCycle;

import java.time.LocalDateTime;

public record PaymentCycleResponseDto(
    Long cycleId,
    String period,
    String status,
    LocalDateTime startDate,
    LocalDateTime dueDate,
    LocalDateTime closedAt,
    String accountName,
    Integer totalMembers,
    Integer monthlyFee,
    Long targetAmount
) {
  public static PaymentCycleResponseDto from(PaymentCycle cycle) {
    return new PaymentCycleResponseDto(
        cycle.getId(),
        cycle.getPeriod(),
        cycle.getStatus(),
        cycle.getStartDate(),
        cycle.getDueDate(),
        cycle.getClosedAt(),
        cycle.getGroup().getAccountName(),
        cycle.getTotalMembers(),
        cycle.getMonthlyFee(),
        (long) cycle.getTotalMembers() * cycle.getMonthlyFee()
    );
  }
}
