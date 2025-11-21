package com.example.capstonedesign20252.chatBot.service;

import com.example.capstonedesign20252.chatBot.dto.ChatResponseDto;
import com.example.capstonedesign20252.group.domain.Group;
import com.example.capstonedesign20252.group.service.GroupService;
import com.example.capstonedesign20252.groupMember.domain.GroupMember;
import com.example.capstonedesign20252.groupMember.repository.GroupMemberRepository;
import com.example.capstonedesign20252.payment.domain.Payment;
import com.example.capstonedesign20252.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

  private final GeminiService geminiService;
  private final PaymentRepository paymentRepository;
  private final GroupMemberRepository groupMemberRepository;  // 🔥 추가!
  private final GroupService groupService;

  // 🤖 시스템 프롬프트
  private static final String SYSTEM_PROMPT = """
            당신은 '오토피봇(Auto Fee Bot)' 동아리 회비 관리 시스템의 AI 어시스턴트 총총이입니다.
            
            주요 기능:
            1. 회비 납부 현황 조회
            2. 미납자 명단 확인
            3. 회비 통계 정보 제공
            4. 엑셀 보고서 생성 안내
            
            답변 규칙:
            - 친근하고 명확하게 답변하세요
            - 한국어로 답변하세요
            - 모르는 내용은 솔직히 모른다고 하세요
            - 불필요하게 길지 않게 답변하세요
            - 질문자는 40~50대 대상이므로 간단하고 명료하게 답변하세요
            """;

  public ChatResponseDto processMessage(Long groupId, String userMessage) {
    Group group = groupService.findByGroupId(groupId);

    log.info("{}번 그룹 {}에서 대화를 시작합니다. 대화 내용: {}",
        group.getId(), group.getGroupName(), userMessage);

    try {
      // 빠른 응답 처리 (키워드 기반)
      ChatResponseDto quickResponse = handleQuickResponse(groupId, userMessage);
      if (quickResponse != null) {
        return quickResponse;
      }

      // Gemini AI 응답
      String aiResponse = geminiService.chat(SYSTEM_PROMPT, userMessage);

      return new ChatResponseDto(aiResponse, "text", null);
    } catch (Exception e) {
      log.error("챗봇 처리 중 오류 발생", e);
      return new ChatResponseDto(
          "죄송합니다. 일시적인 오류가 발생했습니다. 다시 시도해주세요.",
          "text",
          null
      );
    }
  }

  /**
   * 키워드 기반 빠른 응답
   */
  private ChatResponseDto handleQuickResponse(Long groupId, String message) {
    if (message == null) return null;

    String lowerMessage = message.toLowerCase().trim();

    if (lowerMessage.contains("미납") || lowerMessage.contains("안 낸")) {
      return getUnpaidMembers(groupId);
    }

    if (lowerMessage.contains("현황") || lowerMessage.contains("통계")) {
      return getPaymentStatistics(groupId);
    }

    if (lowerMessage.contains("도움") || lowerMessage.contains("help")) {
      return getHelpMessage();
    }

    return null;
  }

  /**
   * 미납자 명단 조회
   * 🔥 수정: Payment 기반으로 미납자 조회
   */
  private ChatResponseDto getUnpaidMembers(Long groupId) {
    Group group = groupService.findByGroupId(groupId);

    // Payment에서 PENDING 상태인 GroupMember 조회
    List<GroupMember> unpaidMembers = paymentRepository.findPendingGroupMemberByGroup(group.getId());

    if (unpaidMembers.isEmpty()) {
      return new ChatResponseDto("모든 회원이 회비를 납부했습니다!", "text", null);
    }

    StringBuilder response = new StringBuilder("**미납자 명단**\n\n");
    for (GroupMember member : unpaidMembers) {
      response.append(String.format("- %s (전화: %s)\n",
          member.getName(),   //
          member.getPhone()   //
      ));
    }

    return new ChatResponseDto(response.toString(), "list", unpaidMembers);
  }

  /**
   * 회비 납부 통계
   * 🔥 수정: Payment 엔티티 기반으로 통계 계산
   */
  private ChatResponseDto getPaymentStatistics(Long groupId) {
    Group group = groupService.findByGroupId(groupId);

    // 1. 그룹의 모든 멤버 수
    List<GroupMember> allMembers = groupMemberRepository.findByGroupId(groupId);

    // 2. 그룹의 모든 Payment 조회
    List<Payment> allPayments = paymentRepository.findByGroupId(groupId);

    if (allPayments.isEmpty()) {
      return new ChatResponseDto(
          "아직 회비 정보가 생성되지 않았습니다.",
          "text",
          null
      );
    }

    // 3. 통계 계산
    long paidCount = allPayments.stream()
                                .filter(p -> "PAID".equals(p.getStatus()))
                                .count();

    long pendingCount = allPayments.stream()
                                   .filter(p -> "PENDING".equals(p.getStatus()))
                                   .count();

    long overdueCount = allPayments.stream()
                                   .filter(p -> "OVERDUE".equals(p.getStatus()))
                                   .count();

    // 총 납부 금액
    double totalPaidAmount = allPayments.stream()
                                        .filter(p -> "PAID".equals(p.getStatus()))
                                        .mapToDouble(p -> p.getAmount().doubleValue())
                                        .sum();

    // 목표 금액 (전체 회비)
    double totalTargetAmount = allPayments.stream()
                                          .mapToDouble(p -> p.getAmount().doubleValue())
                                          .sum();

    // 납부율
    double paymentRate = allPayments.isEmpty() ? 0 : (paidCount * 100.0 / allPayments.size());

    String response = String.format("""
                **회비 납부 현황**
                
                👥 전체 회원: %d명
                ✅ 납부 완료: %d명
                ⏳ 미납: %d명
                ⚠️ 연체: %d명
                
                💰 총 납부 금액: %,d원
                🎯 목표 금액: %,d원
                📊 납부율: %.1f%%
                """,
        allMembers.size(),
        paidCount,
        pendingCount,
        overdueCount,
        (int) totalPaidAmount,
        (int) totalTargetAmount,
        paymentRate
    );

    return new ChatResponseDto(response, "statistics", Map.of(
        "totalMembers", allMembers.size(),
        "paidCount", paidCount,
        "pendingCount", pendingCount,
        "overdueCount", overdueCount,
        "totalPaidAmount", (int) totalPaidAmount,
        "totalTargetAmount", (int) totalTargetAmount,
        "paymentRate", paymentRate
    ));
  }

  /**
   * 도움말 메시지
   */
  private ChatResponseDto getHelpMessage() {
    String helpText = """
                🤖 **오토피봇 사용 가이드**
                
                💡 "미납자 알려줘"
                💡 "회비 현황 보여줘"
                💡 "납부율이 어떻게 돼?"
                💡 "전체 통계 알려줘"
                
                궁금한 점이 있으면 자유롭게 물어보세요! 😊
                """;

    return new ChatResponseDto(helpText, "text", null);
  }
}