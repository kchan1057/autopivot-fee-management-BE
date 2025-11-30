package com.example.capstonedesign20252.fee.dto;

import java.util.List;

public record FeesResponseDto(
    String groupName,
    Integer monthlyFee,
    String paymentPeriod,
    Integer totalMembers,
    Integer paidMembers,
    Integer unpaidMembers,
    Long totalCollected,
    Long targetAmount,
    Integer paymentRate,
    List<MemberPaymentDto> members
) {

}