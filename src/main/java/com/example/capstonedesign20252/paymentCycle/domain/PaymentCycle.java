package com.example.capstonedesign20252.paymentCycle.domain;

import com.example.capstonedesign20252.group.domain.Group;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_cycles")
public class PaymentCycle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  @Column(name = "period", nullable = false, length = 7)
  private String period;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "due_date")
  private LocalDateTime dueDate;

  @Column(name = "closed_at")
  private LocalDateTime closedAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "total_members")
  private Integer totalMembers;

  @Column(name = "monthly_fee")
  private Integer monthlyFee;

  @Builder
  public PaymentCycle(Group group, String period, LocalDateTime dueDate,
      Integer totalMembers, Integer monthlyFee) {
    this.group = group;
    this.period = period;
    this.status = "ACTIVE";
    this.startDate = LocalDateTime.now();
    this.dueDate = dueDate;
    this.totalMembers = totalMembers;
    this.monthlyFee = monthlyFee;
    this.createdAt = LocalDateTime.now();
  }

  public void close() {
    this.status = "CLOSED";
    this.closedAt = LocalDateTime.now();
  }

  public boolean isActive() {
    return "ACTIVE".equals(this.status);
  }
}