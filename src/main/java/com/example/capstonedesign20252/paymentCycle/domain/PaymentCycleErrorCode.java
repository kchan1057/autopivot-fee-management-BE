package com.example.capstonedesign20252.paymentCycle.domain;

import com.example.capstonedesign20252.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentCycleErrorCode implements ErrorCode {

  ALREADY_ACTIVE_CYCLE(HttpStatus.CONFLICT, "CYCLE-001", "이미 진행 중인 수금기간이 존재합니다."),
  NOT_FOUND_CYCLE(HttpStatus.NOT_FOUND, "CYCLE-002", "수금기간을 찾을 수 없습니다."),
  NOT_CYCLE_PERIOD(HttpStatus.BAD_REQUEST, "CYCLE-003", "해당 그룹의 수금 기간이 아닙니다."),
  ALREADY_FINISH_CYCLE(HttpStatus.BAD_REQUEST, "CYCLE-004", "이미 종료된 수금 기간입니다.");


  private final HttpStatus status;
  private final String code;
  private final String message;
}
