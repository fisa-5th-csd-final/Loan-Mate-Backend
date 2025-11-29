package com.fisa.bank.loan.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChangeItem {
  private String type; // "income" or "expense"
  private String name;
  private BigDecimal amount;
}
