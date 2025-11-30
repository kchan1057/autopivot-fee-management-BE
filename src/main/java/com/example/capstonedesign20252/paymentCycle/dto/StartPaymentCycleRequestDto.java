package com.example.capstonedesign20252.paymentCycle.dto;

import java.time.LocalDateTime;

public record StartPaymentCycleRequestDto(
    String period,
    LocalDateTime dueDate
) {
}
