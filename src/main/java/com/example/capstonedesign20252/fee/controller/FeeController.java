package com.example.capstonedesign20252.fee.controller;

import com.example.capstonedesign20252.fee.dto.FeesResponseDto;
import com.example.capstonedesign20252.fee.service.FeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class FeeController {

  private final FeeService feeService;

  @GetMapping("/{groupId}/fees")
  public ResponseEntity<FeesResponseDto> getFees(
      @PathVariable Long groupId,
      @RequestParam(required = false) String period) {

    log.info("회비 현황 조회 요청 - groupId: {}, period: {}", groupId, period);

    // period 없으면 현재 월
    if (period == null || period.isEmpty()) {
      period = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    FeesResponseDto response = feeService.getFeesStatus(groupId, period);
    return ResponseEntity.ok(response);
  }
}
