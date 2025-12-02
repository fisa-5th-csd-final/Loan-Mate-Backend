package com.fisa.bank.account.application.exception;

import com.fisa.bank.common.application.exception.BusinessException;

public class UndefinedAgeGroupException extends BusinessException {
  private static final String ERROR_CODE = "ACC001";

  public UndefinedAgeGroupException() {
    super(ERROR_CODE, "정의되지 않은 연령대입니다");
  }
}
