package com.example.capstonedesigin20252.common.exception;

public record ErrorResponseDto(
    String message, String code, int status
) {
}
