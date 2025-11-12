package com.fisa.bank.common.presentation.response.body;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FailureBody extends ApiResponseBody {

  private final String error;
  private final String message;
}
