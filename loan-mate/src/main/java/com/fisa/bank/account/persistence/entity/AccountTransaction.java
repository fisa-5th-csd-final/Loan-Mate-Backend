package com.fisa.bank.account.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fisa.bank.account.application.domain.TransactionType;

@Entity
@Table(name = "transaction_account")
@Getter
public class AccountTransaction {

  @Id
  @Column(name = "trx_a_id")
  private Long id;

  @Column(name = "account_id")
  private Long accountId;

  @Enumerated(EnumType.STRING)
  private TransactionType type; // WITHDRAW, TRANSFER_OUT 등

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(name = "created_at", insertable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "is_income")
  private Boolean isIncome;
}
