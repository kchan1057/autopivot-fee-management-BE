package com.example.capstonedesigin20252.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public abstract class BaseException extends RuntimeException {

  public abstract String getMessage();
  public abstract String getCode();
  public abstract int getStatus();

}
