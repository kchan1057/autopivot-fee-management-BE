package com.example.capstonedesign20252.group.service;

import com.example.capstonedesign20252.group.domain.Group;
import com.example.capstonedesign20252.group.dto.GroupResponseDto;
import com.example.capstonedesign20252.group.dto.createGroupRequestDto;
import com.example.capstonedesign20252.group.repository.GroupRepository;
import com.example.capstonedesign20252.groupMember.domain.GroupMember;
import com.example.capstonedesign20252.groupMember.repository.GroupMemberRepository;
import com.example.capstonedesign20252.user.domain.User;
import com.example.capstonedesign20252.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final GroupMemberRepository groupMemberRepository;

  /**
   * 그룹 생성 (멤버 없이)
   *
   * 변경사항:
   * - User 정보를 GroupMember에 복사하여 저장
   * - 그룹 생성자를 관리자 멤버로 추가 (isAdmin=true)
   */
  @Transactional
  public GroupResponseDto createGroup(Long userId, createGroupRequestDto dto) {
    // 1. 사용자 조회
    User user = userRepository.findById(userId)
                              .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    // 2. 그룹 생성
    Group group = Group.builder()
                       .user(user)
                       .groupName(dto.groupName())
                       .accountName(dto.accountName())
                       .description(dto.description())
                       .groupCategory(dto.groupCategory())
                       .fee(dto.fee())
                       .build();

    Group savedGroup = groupRepository.save(group);
    log.info("그룹 생성 완료: {} (ID: {})", savedGroup.getGroupName(), savedGroup.getId());

    // 3. 그룹 생성자를 관리자로 추가 (User 정보 복사)
    GroupMember adminMember = GroupMember.builder()
                                         .group(savedGroup)
                                         .name(user.getName())
                                         .email(user.getEmail())
                                         .phone(user.getPhone())
                                         .isAdmin(true)  // 리더
                                         .build();
    groupMemberRepository.save(adminMember);
    log.info("그룹 관리자 추가: {} ({})", user.getName(), user.getEmail());

    return GroupResponseDto.from(savedGroup);
  }

  @Override
  public GroupResponseDto getGroup(Long groupId) {
    Group group = groupRepository.findById(groupId)
                                 .orElseThrow(() -> new IllegalArgumentException("해당 그룹이 존재하지 않습니다."));
    return toDto(group);
  }

  @Override
  public List<GroupResponseDto> getAllGroups() {
    return groupRepository.findAll()
                          .stream()
                          .map(this::toDto)
                          .toList();
  }

  @Override
  public List<GroupResponseDto> getUserGroups(Long userId) {
    return groupRepository.findByUserId(userId)
                          .stream()
                          .map(this::toDto)
                          .toList();
  }

  @Override
  @Transactional
  public void deleteGroup(Long groupId) {
    groupRepository.deleteById(groupId);
  }

  private GroupResponseDto toDto(Group group){
    return new GroupResponseDto(
        group.getId(),
        group.getUser().getId(),
        group.getGroupName(),
        group.getAccountName(),
        group.getDescription(),
        group.getGroupCategory(),
        group.getFee());
  }

  @Override
  public Group findByGroupId(Long groupId){
    return groupRepository.findById(groupId)
                          .orElseThrow(() -> new IllegalArgumentException("해당 그룹이 존재하지 않습니다."));
  }
}