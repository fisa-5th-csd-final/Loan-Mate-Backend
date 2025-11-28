package com.fisa.bank.accountbook.application.exception;

import com.fisa.bank.common.application.exception.BusinessException;

public class ManualLedgerAccessDeniedException extends BusinessException {
  private static final String ERROR_CODE = "ML003";

  public ManualLedgerAccessDeniedException(Long id) {
    super(ERROR_CODE, "해당 수입/지출 내역에 대한 권한이 없습니다. id=" + id);
  }
}
