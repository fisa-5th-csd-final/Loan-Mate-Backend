package com.fisa.bank.common.application.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {

  private final String errorCode;

  public BusinessException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
