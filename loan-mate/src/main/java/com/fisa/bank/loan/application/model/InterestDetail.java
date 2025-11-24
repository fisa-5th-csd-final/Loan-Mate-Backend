package com.fisa.bank.loan.application.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestDetail {
  private LocalDateTime repaymentDate;
  private BigDecimal interest;
}
