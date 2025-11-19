package com.fisa.bank.account.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDetail {
  private Long accountId;
  private String accountNumber;
  private String ownerName;
  private String bankCode;
  private BigDecimal balance;
  private LocalDateTime createdAt;
}
