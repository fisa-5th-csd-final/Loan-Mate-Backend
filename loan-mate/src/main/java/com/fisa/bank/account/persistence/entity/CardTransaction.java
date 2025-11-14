package com.fisa.bank.account.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fisa.bank.account.application.domain.ConsumptionCategory;

// 코어뱅킹 DB 복제 테이블 필요한 필드 매핑한 read-only 엔티티
// 카테고리별 사용 금액 집계에 사용
@Getter
@Entity
@Table(name = "transaction_card")
public class CardTransaction {

  @Id
  @Column(name = "trx_c_id")
  private Long id;

  @Column(name = "account_id")
  private Long accountId; // 연관관계 제거하고 FK를 Long으로 매핑

  @Column(nullable = false)
  private BigDecimal amount;

  //    @Column(name = "store_name")
  //    private String storeName;

  @Enumerated(EnumType.STRING)
  private ConsumptionCategory category;

  @Column(name = "created_at", updatable = false, insertable = false)
  private LocalDateTime createdAt;
}
