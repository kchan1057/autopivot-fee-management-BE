package com.example.capstonedesign20252.groupMember.dto;

import com.example.capstonedesign20252.groupMember.domain.GroupMember;

public record MemberResponseDto(
    Long id,
    String name,
    String email,
    String phone
) {
  public static MemberResponseDto from(GroupMember groupMember){
    return new MemberResponseDto(
        groupMember.getId(),
        groupMember.getName(),
        groupMember.getEmail(),
        groupMember.getPhone()
    );
  }
}