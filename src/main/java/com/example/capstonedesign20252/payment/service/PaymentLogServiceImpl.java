package com.example.capstonedesign20252.payment.service;

import com.example.capstonedesign20252.group.domain.Group;
import com.example.capstonedesign20252.group.repository.GroupRepository;
import com.example.capstonedesign20252.groupMember.domain.GroupMember;
import com.example.capstonedesign20252.groupMember.repository.GroupMemberRepository;
import com.example.capstonedesign20252.payment.domain.Payment;
import com.example.capstonedesign20252.payment.domain.PaymentLog;
import com.example.capstonedesign20252.payment.dto.PaymentRequestDto;
import com.example.capstonedesign20252.payment.repository.PaymentLogRepository;
import com.example.capstonedesign20252.payment.repository.PaymentRepository;
import com.example.capstonedesign20252.paymentCycle.domain.PaymentCycle;
import com.example.capstonedesign20252.paymentCycle.repository.PaymentCycleRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentLogServiceImpl implements PaymentLogService {

  private final PaymentLogRepository paymentLogRepository;
  private final PaymentCycleRepository paymentCycleRepository;
  private final PaymentRepository paymentRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final GroupRepository groupRepository;

  @Transactional
  public void savePaymentLog(PaymentRequestDto requestDto) {
    log.info("입금 알림 수신 - name: {}, amount: {}, accountName: {}",
        requestDto.name(), requestDto.amount(), requestDto.targetAccount());

    PaymentLog paymentLog = PaymentLog.builder()
                                      .name(requestDto.name())
                                      .amount(requestDto.amount())
                                      .targetAccount(requestDto.targetAccount())
                                      .receivedAt(requestDto.receivedAt() != null ? requestDto.receivedAt() : LocalDateTime.now())
                                      .build();
    paymentLogRepository.save(paymentLog);

    log.info("PaymentLog 저장 완료 - logId: {}", paymentLog.getId());

    Optional<Group> groupOpt = groupRepository
        .findByAccountName(requestDto.targetAccount());

    if (groupOpt.isEmpty()) {
      log.info("매칭되는 그룹 없음 - accountName: '{}', 매칭 스킵", requestDto.targetAccount());
      return;
    }

    Group group = groupOpt.get();
    log.info("그룹 매칭 성공 - groupId: {}, groupName: {}", group.getId(), group.getGroupName());

    Optional<PaymentCycle> activeCycleOpt = paymentCycleRepository
        .findByGroupIdAndStatus(group.getId(), "ACTIVE");

    if (activeCycleOpt.isEmpty()) {
      log.info("활성화된 수금 기간 없음 - groupId: {}, 매칭 스킵", group.getId());
      return;
    }

    PaymentCycle cycle = activeCycleOpt.get();
    log.info("수금 기간 매칭 - cycleId: {}, period: {}", cycle.getId(), cycle.getPeriod());

    Optional<GroupMember> memberOpt = groupMemberRepository
        .findByGroupIdAndName(group.getId(), requestDto.name());

    if (memberOpt.isEmpty()) {
      log.warn("멤버 매칭 실패 - groupId: {}, name: '{}'", group.getId(), requestDto.name());
      return;
    }

    GroupMember member = memberOpt.get();
    log.info("멤버 매칭 성공 - memberId: {}, name: {}", member.getId(), member.getName());

    Optional<Payment> paymentOpt = paymentRepository
        .findByGroupMemberIdAndPaymentPeriodAndStatus(
            member.getId(), cycle.getPeriod(), "PENDING");

    if (paymentOpt.isEmpty()) {
      log.warn("PENDING Payment 없음 - memberId: {}, period: {}",
          member.getId(), cycle.getPeriod());
      return;
    }

    Payment payment = paymentOpt.get();

    int requiredAmount = payment.getAmount().intValue();
    int paidAmount = requestDto.amount();

    if (paidAmount >= requiredAmount) {
      LocalDateTime paidAt = requestDto.receivedAt() != null ?
          requestDto.receivedAt() : LocalDateTime.now();
      payment.markAsPaid(paidAt);
      paymentLog.markAsProcessed(payment.getId());

      log.info("납부 완료 처리 - member: {}, amount: {}, paymentId: {}",
          member.getName(), paidAmount, payment.getId());
    } else {
      log.info("부분 납부 감지 - member: {}, paid: {}, required: {}",
          member.getName(), paidAmount, requiredAmount);
    }
  }
}
