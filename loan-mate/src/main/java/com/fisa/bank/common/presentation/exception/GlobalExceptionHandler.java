package com.fisa.bank.common.presentation.exception;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fisa.bank.common.application.exception.BusinessException;
import com.fisa.bank.common.application.exception.ExternalApiException;
import com.fisa.bank.common.presentation.response.ApiResponse;
import com.fisa.bank.common.presentation.response.ApiResponseGenerator;
import com.fisa.bank.common.presentation.response.body.FailureBody;
import com.fisa.bank.common.presentation.response.code.ApiResponseCode.ErrorResponseCode;
import com.fisa.bank.common.presentation.response.code.BusinessErrorCode;
import com.fisa.bank.common.presentation.response.code.NonBusinessErrorCode;

// 애플리케이션 전역 Exception 핸들러
// BusinessException을 제외하고, 다른 종류의 예외들도 추가할 수 있다.
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * 애플리케이션의 공통 Exception인 BusinessException을 캐치하는 Exception 핸들러 BusinessException에, 예외 정보에 대한 구조를
   * 정해서, Http Response를 쉽게 작성할 수 있다.
   *
   * @param e
   * @return
   */
  @ExceptionHandler(BusinessException.class)
  public ApiResponse<FailureBody> handle(BusinessException e) {
    log.warn(e.getMessage(), e);
    ErrorResponseCode<BusinessException> errorCode = BusinessErrorCode.find(e);

    return ApiResponseGenerator.fail(errorCode, e);
  }

  @ExceptionHandler(ExternalApiException.class)
  public ApiResponse<FailureBody> handle(ExternalApiException e) {
    NonBusinessErrorCode errorCode = NonBusinessErrorCode.find(e);
    HttpStatus status = e.getStatus() != null ? e.getStatus() : errorCode.getStatus();
    String code = e.getErrorCode() != null ? e.getErrorCode() : errorCode.getCode();
    log.error("External API error: {} {} body={}", status, e.getMessage(), e.getResponseBody());
    return ApiResponseGenerator.fail(status, code, e.getMessage());
  }

  /**
   * BusinessException 종류가 아닌, 모든 런타임 예외는 아래의 ExceptionHandler가 캐치한다.
   *
   * @param e
   * @return
   */
  @ExceptionHandler(RuntimeException.class)
  public ApiResponse<FailureBody> handle(RuntimeException e) {
    NonBusinessErrorCode errorCode = NonBusinessErrorCode.find(e);
    log.error(e.getMessage(), e);
    return ApiResponseGenerator.fail(
        errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage());
  }
}
