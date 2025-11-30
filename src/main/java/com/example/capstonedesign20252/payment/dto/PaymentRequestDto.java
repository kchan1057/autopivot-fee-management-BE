package com.example.capstonedesign20252.payment.dto;

import com.example.capstonedesign20252.payment.domain.PaymentLog;
import java.time.LocalDateTime;

public record PaymentRequestDto(
    String name,
    Integer amount,
    String targetAccount,
    LocalDateTime receivedAt
) {
  public PaymentLog toEntity() {
    return PaymentLog.builder()
                     .name(this.name)
                     .amount(this.amount)
                     .targetAccount(this.targetAccount)
                     .receivedAt(this.receivedAt != null ? this.receivedAt : LocalDateTime.now())
                     .build();
  }
}
