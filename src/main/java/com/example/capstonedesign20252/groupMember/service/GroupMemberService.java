package com.example.capstonedesign20252.groupMember.service;

import com.example.capstonedesign20252.excel.dto.MemberDataDto;
import com.example.capstonedesign20252.group.domain.Group;
import com.example.capstonedesign20252.groupMember.domain.GroupMember;
import com.example.capstonedesign20252.group.repository.GroupRepository;
import com.example.capstonedesign20252.groupMember.repository.GroupMemberRepository;
import com.example.capstonedesign20252.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupMemberService {

  private final GroupRepository groupRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final UserRepository userRepository;

  /**
   * 그룹 관리자 권한 확인
   *
   * 변경: userId를 받아서 해당 User가 만든 그룹의 관리자인지 확인
   */
  public void validateGroupLeader(Long groupId, Long userId) {
    Group group = groupRepository.findById(groupId)
                                 .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

    // Group의 생성자(user_id)와 요청자가 같은지 확인
    if (!group.getUser().getId().equals(userId)) {
      throw new IllegalStateException("그룹 관리자만 멤버를 추가할 수 있습니다.");
    }
  }

  /**
   * 엑셀 데이터로 멤버 추가
   *
   * 변경: User 생성 없이 GroupMember에 정보만 저장
   */
  @Transactional
  public int addMembersFromExcel(Long groupId, List<MemberDataDto> memberDataList) {
    Group group = groupRepository.findById(groupId)
                                 .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

    int addedCount = 0;

    for (MemberDataDto data : memberDataList) {
      try {
        // 이메일 또는 전화번호로 이미 존재하는 멤버인지 확인
        boolean alreadyExists = false;

        if (data.email() != null && !data.email().isEmpty()) {
          alreadyExists = groupMemberRepository.existsByGroupIdAndEmail(groupId, data.email());
        }

        if (!alreadyExists && data.phone() != null && !data.phone().isEmpty()) {
          alreadyExists = groupMemberRepository.existsByGroupIdAndPhone(groupId, data.phone());
        }

        if (alreadyExists) {
          log.warn("이미 그룹에 존재하는 멤버입니다: {} ({})", data.name(), data.email());
          continue;
        }

        // 🔥 User 생성 없이 바로 GroupMember에 정보 저장!
        GroupMember member = GroupMember.builder()
                                        .group(group)
                                        .name(data.name())
                                        .email(data.email())
                                        .phone(data.phone())
                                        .isAdmin(false)  // 일반 멤버
                                        .build();

        groupMemberRepository.save(member);
        addedCount++;
        log.debug("멤버 추가: {} ({})", data.name(), data.email());

      } catch (Exception e) {
        log.error("멤버 추가 실패: {} - {}", data.name(), e.getMessage());
        // 개별 실패는 로그만 남기고 계속 진행
      }
    }

    log.info("그룹 {} 멤버 추가 완료: {}명", groupId, addedCount);
    return addedCount;
  }

  /**
   * 멤버 목록 조회
   */
  public List<GroupMember> getGroupMembers(Long groupId) {
    return groupMemberRepository.findByGroupId(groupId);
  }

  /**
   * 멤버 삭제
   */
  @Transactional
  public void removeMember(Long groupId, Long memberId, Long requesterId) {
    validateGroupLeader(groupId, requesterId);

    GroupMember member = groupMemberRepository
        .findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("그룹 멤버를 찾을 수 없습니다."));

    if (!member.getGroup().getId().equals(groupId)) {
      throw new IllegalArgumentException("해당 그룹의 멤버가 아닙니다.");
    }

    if (member.getIsAdmin()) {
      throw new IllegalStateException("그룹 관리자는 삭제할 수 없습니다.");
    }

    groupMemberRepository.delete(member);
  }
}