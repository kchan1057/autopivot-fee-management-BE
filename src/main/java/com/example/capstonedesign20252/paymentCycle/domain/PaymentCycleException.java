package com.example.capstonedesign20252.paymentCycle.domain;

import com.example.capstonedesign20252.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class PaymentCycleException extends BaseException {

  private final PaymentCycleErrorCode paymentCycleErrorCode;

  @Override
  public String getMessage() {
    return paymentCycleErrorCode.getMessage();
  }

  @Override
  public String getCode() {
    return paymentCycleErrorCode.getCode();
  }

  @Override
  public int getStatus() {
    return paymentCycleErrorCode.getStatus().value();
  }
}
