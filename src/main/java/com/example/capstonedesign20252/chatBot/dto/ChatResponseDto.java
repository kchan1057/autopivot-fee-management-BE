package com.example.capstonedesign20252.chatBot.dto;

public record ChatResponseDto(
  String response,
  String type, // "text", "excel", "list" 등
  Object data // 추가 데이터 (엑셀 URL, 리스트 등)
){
}