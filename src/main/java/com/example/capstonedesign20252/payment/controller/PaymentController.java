package com.example.capstonedesign20252.payment.controller;

import com.example.capstonedesign20252.payment.domain.Payment;
import com.example.capstonedesign20252.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

  private final PaymentRepository paymentRepository;

  // 납부 확인 (수동)
  @PostMapping("/{paymentId}/confirm")
  public ResponseEntity<Map<String, Object>> confirmPayment(@PathVariable Long paymentId) {
    log.info("납부 확인 요청 - paymentId: {}", paymentId);

    Payment payment = paymentRepository.findById(paymentId)
                                       .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다. paymentId: " + paymentId));

    // 이미 납부된 경우 체크
    if ("PAID".equals(payment.getStatus())) {
      log.warn("이미 납부 확인된 건 - paymentId: {}", paymentId);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", "이미 납부 확인된 건입니다.",
          "paymentId", paymentId,
          "paidAt", payment.getPaidAt()
      ));
    }

    // 납부 확인 처리
    payment.manualPaid();  // status = PAID, paidAt = now()
    paymentRepository.save(payment);

    log.info("납부 확인 완료 - paymentId: {}, paidAt: {}", paymentId, payment.getPaidAt());

    return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "납부가 확인되었습니다.",
        "paymentId", paymentId,
        "paidAt", payment.getPaidAt()
    ));
  }
}