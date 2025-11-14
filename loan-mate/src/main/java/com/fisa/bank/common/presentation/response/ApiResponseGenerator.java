package com.fisa.bank.common.presentation.response;

import org.springframework.http.HttpStatus;

import com.fisa.bank.common.application.exception.BusinessException;
import com.fisa.bank.common.presentation.response.body.FailureBody;
import com.fisa.bank.common.presentation.response.body.SuccessBody;
import com.fisa.bank.common.presentation.response.code.ApiResponseCode.ErrorResponseCode;
import com.fisa.bank.common.presentation.response.code.ApiResponseCode.SuccessResponseCode;
import com.fisa.bank.common.presentation.response.code.MessageCode;

public class ApiResponseGenerator {

  /**
   * @param responseCode
   * @param body
   * @return
   * @param <T>
   */
  public static <T> ApiResponse<SuccessBody<T>> success(SuccessResponseCode responseCode, T body) {

    String code = responseCode.getCode();
    String message = responseCode.getMessage();

    return new ApiResponse<>(responseCode.getStatus(), new SuccessBody<>(code, message, body));
  }

  public static <T> ApiResponse<SuccessBody<T>> success(SuccessResponseCode responseCode) {
    String code = responseCode.getCode();
    String message = responseCode.getMessage();

    return new ApiResponse<>(responseCode.getStatus(), new SuccessBody<>(code, message, null));
  }

  public static <T> ApiResponse<SuccessBody<T>> success(
      HttpStatus status, MessageCode messageCode, T body) {
    String code = messageCode.getCode();
    String message = messageCode.getMessage();
    return new ApiResponse<>(status, new SuccessBody<>(code, message, body));
  }

  /**
   * BusinessException 과 ErrorResponseCode를 사용해서 응답 객체를 생성합니다.
   *
   * @param responseCode
   * @param e
   * @return
   */
  public static ApiResponse<FailureBody> fail(
      ErrorResponseCode<? extends Throwable> responseCode, BusinessException e) {
    HttpStatus status = responseCode.getStatus();
    String errorCode = e.getErrorCode();
    String message = e.getMessage();
    return new ApiResponse<>(status, new FailureBody(errorCode, message));
  }

  /**
   * BusinessException이 아닌, status, errorCode, message를 전부 받아서 응답 객체를 생성합니다.
   *
   * @param status
   * @param errorCode
   * @param message
   * @return
   */
  public static ApiResponse<FailureBody> fail(HttpStatus status, String errorCode, String message) {
    return new ApiResponse<>(status, new FailureBody(errorCode, message));
  }
}
