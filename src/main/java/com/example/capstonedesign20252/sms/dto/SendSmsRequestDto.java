package com.example.capstonedesign20252.sms.dto;

public record SendSmsRequestDto(
    Long memberId,
    String phone,
    String message
) {}