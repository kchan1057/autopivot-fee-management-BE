package com.example.capstonedesign20252.groupMember.repository;

import com.example.capstonedesign20252.groupMember.domain.GroupMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

  /**
   * 그룹의 모든 멤버 조회
   */
  List<GroupMember> findByGroupId(Long groupId);

  /**
   * 그룹 내에서 이메일 또는 전화번호로 멤버 존재 여부 확인
   */
  boolean existsByGroupIdAndEmail(Long groupId, String email);
  boolean existsByGroupIdAndPhone(Long groupId, String phone);

  /**
   * 그룹 내에서 이메일로 멤버 조회
   */
  Optional<GroupMember> findByGroupIdAndEmail(Long groupId, String email);

  /**
   * 그룹의 관리자 조회
   */
  Optional<GroupMember> findByGroupIdAndIsAdmin(Long groupId, Boolean isAdmin);
}