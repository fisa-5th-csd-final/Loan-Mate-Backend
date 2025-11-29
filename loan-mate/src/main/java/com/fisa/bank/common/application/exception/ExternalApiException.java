package com.fisa.bank.common.application.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class ExternalApiException extends RuntimeException {
  private final HttpStatus status;
  private final String errorCode;
  private final String responseBody;

  public ExternalApiException(
      HttpStatus status, String errorCode, String message, String responseBody) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
    this.responseBody = responseBody;
  }
}
