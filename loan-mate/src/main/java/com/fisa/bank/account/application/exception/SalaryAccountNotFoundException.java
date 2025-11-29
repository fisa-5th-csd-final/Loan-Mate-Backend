package com.fisa.bank.account.application.exception;

import com.fisa.bank.common.application.exception.BusinessException;

public class SalaryAccountNotFoundException extends BusinessException {
  private static final String ERROR_CODE = "ACC001";

  public SalaryAccountNotFoundException(Long userId) {
    super(ERROR_CODE, "급여 통장을 찾을 수 없습니다. userId=" + userId);
  }
}
