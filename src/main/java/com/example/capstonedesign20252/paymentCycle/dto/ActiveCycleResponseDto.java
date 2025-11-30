package com.example.capstonedesign20252.paymentCycle.dto;

import java.time.LocalDateTime;

public record ActiveCycleResponseDto(
    boolean hasActiveCycle,
    Long cycleId,
    String period,
    LocalDateTime startDate,
    LocalDateTime dueDate,
    String accountName,
    Integer totalMembers,
    Integer monthlyFee,
    Long targetAmount,
    Integer paidMembers,
    Integer unpaidMembers,
    Long totalCollected,
    Integer paymentRate
) {
}
