package com.fisa.bank.loan.application.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class LoanProgressResponse {
  private final Long loanId;
  private final String name;
  private Integer progress;

  public static LoanProgressResponse from(Long loanId, String name, Integer progress) {

    return new LoanProgressResponse(loanId, name, progress);
  }
}
