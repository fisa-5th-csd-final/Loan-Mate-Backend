package com.fisa.bank.common.presentation.response.code;

import org.springframework.http.HttpStatus;

// TODO: HttpStatus 의존성 제거하기
public interface ApiResponseCode {

  public HttpStatus getStatus();

  public String getCode();

  public String getMessage();

  /**
   * 정상 응답과 예외 발생 시 응답 구조는 동일하게 설계
   *
   * @param <T>
   */
  public static interface ErrorResponseCode<T extends Throwable> extends ApiResponseCode {}

  public static interface SuccessResponseCode extends ApiResponseCode {}
}
