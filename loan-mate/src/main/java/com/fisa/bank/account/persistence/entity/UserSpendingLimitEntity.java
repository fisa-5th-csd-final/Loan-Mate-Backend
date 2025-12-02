package com.fisa.bank.account.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import com.fisa.bank.common.persistence.entity.BaseEntity;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

@Entity
@Table(
    name = "user_spending_limit",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "ux_user_spending_limit_user",
          columnNames = {"service_user_id"})
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserSpendingLimitEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "service_user_id", nullable = false)
  private Long serviceUserId;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "user_spending_limit_ratio",
      joinColumns = @JoinColumn(name = "user_spending_limit_id"))
  @MapKeyColumn(name = "category")
  @MapKeyEnumerated(EnumType.STRING)
  @Column(name = "ratio", nullable = false, precision = 10, scale = 4)
  @Builder.Default
  private Map<ConsumptionCategory, BigDecimal> limits = new EnumMap<>(ConsumptionCategory.class);

  public void updateLimits(Map<ConsumptionCategory, BigDecimal> updated) {
    limits.clear();
    if (updated != null) {
      limits.putAll(updated);
    }
  }
}
