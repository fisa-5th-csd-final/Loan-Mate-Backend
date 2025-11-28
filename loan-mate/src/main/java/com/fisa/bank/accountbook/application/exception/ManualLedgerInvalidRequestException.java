package com.fisa.bank.accountbook.application.exception;

import com.fisa.bank.common.application.exception.BusinessException;

public class ManualLedgerInvalidRequestException extends BusinessException {
  private static final String ERROR_CODE = "ML001";

  public ManualLedgerInvalidRequestException(String message) {
    super(ERROR_CODE, message);
  }
}
