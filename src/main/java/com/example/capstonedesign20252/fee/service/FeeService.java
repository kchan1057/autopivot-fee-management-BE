package com.example.capstonedesign20252.fee.service;

import com.example.capstonedesign20252.fee.dto.FeesResponseDto;
import com.example.capstonedesign20252.fee.dto.MemberPaymentDto;
import com.example.capstonedesign20252.group.domain.Group;
import com.example.capstonedesign20252.group.repository.GroupRepository;
import com.example.capstonedesign20252.groupMember.domain.GroupMember;
import com.example.capstonedesign20252.groupMember.repository.GroupMemberRepository;
import com.example.capstonedesign20252.payment.domain.Payment;
import com.example.capstonedesign20252.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeeService {

  private final GroupRepository groupRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final PaymentRepository paymentRepository;

  public FeesResponseDto getFeesStatus(Long groupId, String period) {
    // 1. 그룹 조회
    Group group = groupRepository.findById(groupId)
                                 .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다. groupId: " + groupId));

    // 2. 그룹 멤버 조회
    List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);

    // 3. 해당 월의 Payment 조회
    List<Payment> payments = paymentRepository.findByGroupIdAndPaymentPeriod(groupId, period);

    // 4. 멤버별 Payment 매핑 (memberId -> Payment)
    Map<Long, Payment> paymentMap = payments.stream()
                                            .collect(Collectors.toMap(
                                                p -> p.getGroupMember().getId(),
                                                p -> p,
                                                (p1, p2) -> p1  // 중복 시 첫 번째 사용
                                            ));

    // 5. 현재 날짜 (연체 판단용)
    LocalDateTime now = LocalDateTime.now();

    // 6. 멤버별 납부 현황 생성
    List<MemberPaymentDto> memberPayments = members.stream()
                                                   .map(member -> {
                                                     Payment payment = paymentMap.get(member.getId());

                                                     if (payment == null) {
                                                       // Payment 레코드가 없으면 PENDING으로 처리
                                                       return new MemberPaymentDto(
                                                           member.getId(),
                                                           null,  // paymentId 없음
                                                           member.getName(),
                                                           member.getPhone(),
                                                           0,
                                                           "PENDING",
                                                           null
                                                       );
                                                     }

                                                     // status 결정 로직
                                                     String status = payment.getStatus();

                                                     // PENDING인데 dueDate가 지났으면 OVERDUE로 변경
                                                     if ("PENDING".equals(status) && payment.getDueDate() != null
                                                         && payment.getDueDate().isBefore(now)) {
                                                       status = "OVERDUE";
                                                     }

                                                     return new MemberPaymentDto(
                                                         member.getId(),
                                                         payment.getId(),
                                                         member.getName(),
                                                         member.getPhone(),
                                                         payment.getAmount().intValue(),
                                                         status,
                                                         payment.getPaidAt()
                                                     );
                                                   })
                                                   .collect(Collectors.toList());

    // 7. 통계 계산
    int totalMembers = members.size();
    int paidMembers = (int) memberPayments.stream()
                                          .filter(m -> "PAID".equals(m.status()))
                                          .count();
    int unpaidMembers = totalMembers - paidMembers;

    long totalCollected = memberPayments.stream()
                                        .filter(m -> "PAID".equals(m.status()))
                                        .mapToLong(MemberPaymentDto::paidAmount)
                                        .sum();

    long targetAmount = (long) group.getFee() * totalMembers;
    int paymentRate = totalMembers == 0 ? 0 : (int) ((paidMembers * 100) / totalMembers);

    log.info("회비 현황 조회 완료 - 납부율: {}%, 납부: {}명, 미납: {}명",
        paymentRate, paidMembers, unpaidMembers);

    return new FeesResponseDto(
        group.getGroupName(),
        group.getFee(),
        period,
        totalMembers,
        paidMembers,
        unpaidMembers,
        totalCollected,
        targetAmount,
        paymentRate,
        memberPayments
    );
  }
}
