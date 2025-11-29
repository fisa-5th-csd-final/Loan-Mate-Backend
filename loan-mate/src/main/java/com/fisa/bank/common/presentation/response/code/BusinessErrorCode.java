package com.fisa.bank.common.presentation.response.code;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;

import com.fisa.bank.account.application.exception.SalaryAccountNotFoundException;
import com.fisa.bank.accountbook.application.exception.ManualLedgerAccessDeniedException;
import com.fisa.bank.accountbook.application.exception.ManualLedgerNotFoundException;
import com.fisa.bank.common.application.exception.AlreadyDeletedException;
import com.fisa.bank.common.application.exception.BusinessException;
import com.fisa.bank.common.presentation.response.code.ApiResponseCode.ErrorResponseCode;

public enum BusinessErrorCode implements ErrorResponseCode<BusinessException> {

  /** 여기에 커스텀 BusinessException을 정의하면 됩니다. */
  // Common
  ALREADY_DELETED_EXCEPTION(HttpStatus.NOT_FOUND, AlreadyDeletedException.class),
  MANUAL_LEDGER_NOT_FOUND(HttpStatus.NOT_FOUND, ManualLedgerNotFoundException.class),
  MANUAL_LEDGER_ACCESS_DENIED(HttpStatus.FORBIDDEN, ManualLedgerAccessDeniedException.class),
  SALARY_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, SalaryAccountNotFoundException.class);

  private final HttpStatus status;
  @Getter private final Class<? extends BusinessException> exception;

  BusinessErrorCode(HttpStatus status, Class<? extends BusinessException> eClass) {
    this.status = status;
    this.exception = eClass;
  }

  @Override
  public HttpStatus getStatus() {
    return this.status;
  }

  @Override
  public String getCode() {
    throw new UnsupportedOperationException(
        "You should use errorCode, message in the BusinessException");
  }

  @Override
  public String getMessage() {
    throw new UnsupportedOperationException(
        "You should use errorCode, message in the BusinessException");
  }

  public static ErrorResponseCode<BusinessException> find(BusinessException exception) {
    BusinessErrorCode errorResponseCode = map.get(exception.getClass());

    if (errorResponseCode != null) return errorResponseCode;

    throw new IllegalArgumentException("Not mapped Exception");
  }

  /** 커스텀 Exception에 맞는 ErrorCode를 매핑하는 저장소 */
  private static final Map<Class<? extends BusinessException>, BusinessErrorCode> map =
      new ConcurrentHashMap<>();

  static {
    Arrays.stream(BusinessErrorCode.values())
        .forEach(
            errorCode -> {
              Class<? extends BusinessException> eClass = errorCode.exception;
              map.put(eClass, errorCode);
            });
  }
}
