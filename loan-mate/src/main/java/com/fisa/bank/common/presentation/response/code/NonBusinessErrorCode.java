package com.fisa.bank.common.presentation.response.code;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;

import com.fisa.bank.common.application.exception.ExternalApiException;
import com.fisa.bank.common.presentation.response.code.ApiResponseCode.ErrorResponseCode;

@Getter
public enum NonBusinessErrorCode implements ErrorResponseCode<Throwable> {
  EXTERNAL_API_ERROR(
      HttpStatus.BAD_GATEWAY,
      "EXTERNAL_API_ERROR",
      "External API 호출 실패",
      ExternalApiException.class),
  INTERNAL_SERVER_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "INTERNAL_SERVER_ERROR",
      "알 수 없는 서버 오류",
      RuntimeException.class);

  private final HttpStatus status;
  private final String code;
  private final String message;
  private final Class<? extends Throwable> exception;

  NonBusinessErrorCode(
      HttpStatus status, String code, String message, Class<? extends Throwable> exception) {
    this.status = status;
    this.code = code;
    this.message = message;
    this.exception = exception;
  }

  public static NonBusinessErrorCode find(Throwable exception) {
    Class<?> target = exception.getClass();
    // 우선 등록된 예외 클래스와 assignable 한 항목을 찾는다.
    return map.entrySet().stream()
        .filter(entry -> entry.getKey().isAssignableFrom(target))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(INTERNAL_SERVER_ERROR);
  }

  private static final Map<Class<? extends Throwable>, NonBusinessErrorCode> map =
      new ConcurrentHashMap<>();

  static {
    Arrays.stream(NonBusinessErrorCode.values())
        .forEach(errorCode -> map.put(errorCode.exception, errorCode));
  }
}
