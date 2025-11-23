package com.example.capstonedesign20252.groupMember.domain;

import com.example.capstonedesign20252.common.BaseEntity;
import com.example.capstonedesign20252.group.domain.Group;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMember extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JoinColumn(name = "group_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Group group;

  // 🔥 User 참조 제거! 멤버 정보를 직접 저장
  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "email")
  private String email;

  @Column(name = "phone")
  private String phone;

  // 리더(그룹 생성자)인지 구분
  @Column(name = "is_admin", nullable = false)
  private Boolean isAdmin;

  @Builder
  public GroupMember(Group group, String name, String email, String phone, Boolean isAdmin){
    this.group = group;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.isAdmin = isAdmin != null ? isAdmin : false;
  }
}