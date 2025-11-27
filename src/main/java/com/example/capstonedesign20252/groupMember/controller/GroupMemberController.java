package com.example.capstonedesign20252.groupMember.controller;

import com.example.capstonedesign20252.excel.service.ExcelParserService;
import com.example.capstonedesign20252.excel.dto.MemberDataDto;
import com.example.capstonedesign20252.groupMember.domain.GroupMember;
import com.example.capstonedesign20252.groupMember.dto.AddGroupMemberDto;
import com.example.capstonedesign20252.groupMember.dto.MemberResponseDto;
import com.example.capstonedesign20252.groupMember.dto.UpdateGroupMemberDto;
import com.example.capstonedesign20252.groupMember.service.GroupMemberService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupMemberController {

  private final ExcelParserService excelParserService;
  private final GroupMemberService groupMemberService;

  @GetMapping("/{groupId}/members")
  public ResponseEntity<List<MemberResponseDto>> getGroupMembers(
      @PathVariable Long groupId
  ){
    List<GroupMember> members = groupMemberService.getGroupMembers(groupId);
    List<MemberResponseDto> response = members.stream()
                                              .map(MemberResponseDto::from)
                                              .toList();
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{groupId}/members")
  public ResponseEntity<MemberResponseDto> addGroupMember(
      @PathVariable Long groupId,
      @RequestBody AddGroupMemberDto addGroupMemberDto
  ){
    return ResponseEntity.ok(groupMemberService.addGroupMember(groupId, addGroupMemberDto));
  }

  @PutMapping("/{groupId}/members/{memberId}")
  public ResponseEntity<MemberResponseDto> updateGroupMember(
      @PathVariable Long groupId,
      @PathVariable Long memberId,
      @RequestBody UpdateGroupMemberDto updateGroupMemberDto
  ){
    return ResponseEntity.ok(groupMemberService.updateGroupMember(groupId, memberId, updateGroupMemberDto));
  }

  @DeleteMapping("/{groupId}/members/{memberId}")
  public ResponseEntity<Void> deleteGroupMember(
      @PathVariable Long groupId,
      @PathVariable Long memberId,
      @AuthenticationPrincipal UserDetails userDetails
  ){
    Long userId = Long.parseLong(userDetails.getUsername());
    groupMemberService.removeMember(groupId, memberId, userId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{groupId}/members/upload")
  public ResponseEntity<?> uploadMembers(
      @PathVariable Long groupId,
      @RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal UserDetails userDetails) {

    Long userId = Long.parseLong(userDetails.getUsername());
    log.info("그룹 {} 멤버 업로드 시작 - 파일: {}, 요청자: {}", groupId, file.getOriginalFilename(), userId);

    try {
      groupMemberService.validateGroupLeader(groupId, userId);
      List<MemberDataDto> memberDataList = excelParserService.parseExcelFile(file);

      if (memberDataList.isEmpty()) {
        return ResponseEntity.badRequest()
                             .body(new ErrorResponse("유효한 멤버 데이터가 없습니다."));
      }

      int addedCount = groupMemberService.addMembersFromExcel(groupId, memberDataList);
      log.info("그룹 {} 멤버 {}명 추가 완료", groupId, addedCount);

      return ResponseEntity.ok(new MemberUploadResponse(
          addedCount,
          "멤버가 성공적으로 추가되었습니다."
      ));

    } catch (IOException e) {
      log.error("엑셀 파일 파싱 오류: {}", e.getMessage());
      return ResponseEntity.badRequest()
                           .body(new ErrorResponse("엑셀 파일을 읽을 수 없습니다."));
    } catch (IllegalStateException e) {
      log.error("권한 오류: {}", e.getMessage());
      return ResponseEntity.status(403)
                           .body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      log.error("멤버 업로드 오류: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError()
                           .body(new ErrorResponse("멤버 추가 중 오류가 발생했습니다."));
    }
  }

  record MemberUploadResponse(int count, String message) {}
  record ErrorResponse(String message) {}
}
