package com.fisa.bank.common.application.exception;

public class AlreadyDeletedException extends BusinessException {
  private static final String errorCode = "C001";
  private static final String message = "%s (ID: %s) 는 이미 삭제된 상태입니다.";

  public AlreadyDeletedException(String entityName, Long id) {
    super(errorCode, String.format(message, entityName, id));
  }
}
