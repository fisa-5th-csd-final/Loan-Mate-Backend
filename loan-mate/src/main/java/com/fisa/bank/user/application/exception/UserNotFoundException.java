package com.fisa.bank.user.application.exception;

import com.fisa.bank.common.application.exception.BusinessException;

public class UserNotFoundException extends BusinessException {
  private static final String errorCode = "U001";
  private static final String message = "해당 사용자를 찾을 수 없습니다.";

  public UserNotFoundException() {
    super(errorCode, message);
  }
}
