package com.fisa.bank.accountbook.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.common.persistence.entity.BaseEntity;

@Entity
@Table(name = "manual_ledger")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ManualLedgerEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long serviceUserId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ManualLedgerType type;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(length = 255)
  private String description;

  @Column(nullable = false)
  private LocalDate savedAt;
}
