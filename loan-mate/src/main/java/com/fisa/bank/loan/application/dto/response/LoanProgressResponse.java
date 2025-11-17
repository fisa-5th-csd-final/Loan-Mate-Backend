package com.fisa.bank.loan.application.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class LoanProgressResponse {
  private final Long loanId;
  private final String name;
  private BigDecimal progress;

  public static LoanProgressResponse from(Long loanId, String name, BigDecimal progress) {

    return new LoanProgressResponse(loanId, name, progress);
  }
}
