package com.fisa.bank.accountbook.application.exception;

import com.fisa.bank.common.application.exception.BusinessException;

public class ManualLedgerNotFoundException extends BusinessException {
  private static final String ERROR_CODE = "ML002";

  public ManualLedgerNotFoundException(Long id) {
    super(ERROR_CODE, "수입/지출 내역을 찾을 수 없습니다. id=" + id);
  }
}
