package com.fisa.bank.common.presentation.response.body;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SuccessBody<T> extends ApiResponseBody {

  private final String code;
  private final String message;
  private final T data;
}
