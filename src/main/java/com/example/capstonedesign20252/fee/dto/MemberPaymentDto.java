package com.example.capstonedesign20252.fee.dto;

import java.time.LocalDateTime;

public record MemberPaymentDto(
    Long memberId,
    Long paymentId,
    String name,
    String phone,
    Integer paidAmount,
    String status,
    LocalDateTime paidAt
) {}