package com.fisa.bank.loan.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.fisa.bank.loan.persistence.enums.ChangeItemType;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChangeItem {
  private ChangeItemType type; // "income" or "expense"
  private String name;
  private BigDecimal amount;
}
