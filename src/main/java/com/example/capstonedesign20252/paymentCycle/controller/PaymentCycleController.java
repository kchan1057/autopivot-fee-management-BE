package com.example.capstonedesign20252.paymentCycle.controller;


import com.example.capstonedesign20252.paymentCycle.dto.ActiveCycleResponseDto;
import com.example.capstonedesign20252.paymentCycle.dto.PaymentCycleResponseDto;
import com.example.capstonedesign20252.paymentCycle.dto.StartPaymentCycleRequestDto;
import com.example.capstonedesign20252.paymentCycle.service.PaymentCycleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/payment-cycles")
public class PaymentCycleController {

  private final PaymentCycleService paymentCycleService;

  @PostMapping("/start")
  public ResponseEntity<PaymentCycleResponseDto> startPaymentCycle(
      @PathVariable Long groupId,
      @RequestBody StartPaymentCycleRequestDto request) {

    log.info("회비 수금 시작 요청 - groupId: {}, period: {}", groupId, request.period());
    return ResponseEntity.ok(paymentCycleService.startPaymentCycle(groupId, request));
  }

  @PostMapping("/{cycleId}/close")
  public ResponseEntity<PaymentCycleResponseDto> closePaymentCycle(
      @PathVariable Long groupId,
      @PathVariable Long cycleId
  ){
    log.info("회비 수금 종료 요청 - groupId: {}, cycleId: {}", groupId, cycleId);
    return ResponseEntity.ok(paymentCycleService.closePaymentCycle(groupId, cycleId));
  }

  @GetMapping("/active")
  public ResponseEntity<ActiveCycleResponseDto> getActiveCycle(
      @PathVariable Long groupId
  ){
    log.info("활성 수금 기간 조회 - groupId: {}", groupId);
    return ResponseEntity.ok(paymentCycleService.getActiveCycle(groupId));
  }

  @GetMapping
  public ResponseEntity<List<PaymentCycleResponseDto>> getCycleHistory(
      @PathVariable Long groupId
  ){
    log.info("수금 기간 히스토리 조회 - groupId: {}", groupId);
    return ResponseEntity.ok(paymentCycleService.getCycleHistory(groupId));
  }
}
