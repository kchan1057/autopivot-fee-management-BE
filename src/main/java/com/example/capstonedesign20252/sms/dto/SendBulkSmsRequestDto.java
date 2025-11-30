package com.example.capstonedesign20252.sms.dto;

import java.util.List;

public record SendBulkSmsRequestDto(
    List<Long> memberIds,
    String message
) {}