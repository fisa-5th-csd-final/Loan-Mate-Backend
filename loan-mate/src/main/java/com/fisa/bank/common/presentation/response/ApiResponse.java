package com.fisa.bank.common.presentation.response;

import lombok.Getter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fisa.bank.common.presentation.response.body.ApiResponseBody;

@Getter
public class ApiResponse<B extends ApiResponseBody> extends ResponseEntity<B> {

  private final B body;

  /**
   * Spring이 제공하는 HttpStatus를 사용하여 응답을 생성하는 생성자
   *
   * @param statusCode
   */
  public ApiResponse(HttpStatus statusCode) {
    super(statusCode);
    this.body = null;
  }

  /**
   * Spring이 제공하는 HttpStatus를 사용하고, ApiResponseBody를 사용하여 응답을 생성하는 생성자
   *
   * @param statusCode
   * @param body
   */
  public ApiResponse(HttpStatus statusCode, B body) {
    super(statusCode);
    this.body = body;
  }

  /**
   * Spring이 제공하는 HttpStatus를 사용하지 않고, int와 ApiResponseBody를 사용하여 응답을 생성하는 생성자
   *
   * @param status
   * @param body
   * @throws IllegalArgumentException
   */
  public ApiResponse(int status, B body) {
    super(HttpStatus.valueOf(status));
    this.body = body;
  }

  /**
   * Spring이 제공하는 HttpStatus를 사용하지 않고, int를 사용하여 응답을 생성하는 생성자
   *
   * @param status
   */
  public ApiResponse(int status) {
    super(HttpStatus.valueOf(status));
    this.body = null;
  }
}
