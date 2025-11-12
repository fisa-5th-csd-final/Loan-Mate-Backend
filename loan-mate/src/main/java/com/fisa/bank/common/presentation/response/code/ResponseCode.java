package com.fisa.bank.common.presentation.response.code;

import org.springframework.http.HttpStatus;

import com.fisa.bank.common.presentation.response.code.ApiResponseCode.SuccessResponseCode;

public class ResponseCode implements SuccessResponseCode {

  public static final ResponseCode CREATE =
      new ResponseCode(HttpStatus.CREATED, MessageCode.CREATE);
  public static final ResponseCode UPDATE = new ResponseCode(HttpStatus.OK, MessageCode.UPDATE);
  public static final ResponseCode DELETE =
      new ResponseCode(HttpStatus.NO_CONTENT, MessageCode.DELETE);
  public static final ResponseCode GET = new ResponseCode(HttpStatus.OK, MessageCode.GET);

  private final HttpStatus status;
  private final MessageCode messageCode;

  public ResponseCode(HttpStatus status, MessageCode messageCode) {
    this.messageCode = messageCode;
    this.status = status;
  }

  @Override
  public HttpStatus getStatus() {
    return this.status;
  }

  @Override
  public String getCode() {
    return this.messageCode.getCode();
  }

  @Override
  public String getMessage() {
    return this.messageCode.getMessage();
  }
}
