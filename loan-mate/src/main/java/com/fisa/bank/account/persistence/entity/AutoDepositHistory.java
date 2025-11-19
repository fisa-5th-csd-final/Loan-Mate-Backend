package com.fisa.bank.account.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "auto_deposit_history")
public class AutoDepositHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long loanLedgerId;

  private BigDecimal amount;

  private String status; // SUCCESS / FAILED

  private String message;

  private LocalDateTime executedAt;
}
