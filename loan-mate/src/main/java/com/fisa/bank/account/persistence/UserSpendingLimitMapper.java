package com.fisa.bank.account.persistence;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fisa.bank.account.application.model.UserSpendingLimit;
import com.fisa.bank.account.persistence.entity.UserSpendingLimitEntity;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

@Component
public class UserSpendingLimitMapper {

  public UserSpendingLimit toDomain(UserSpendingLimitEntity entity) {
    if (entity == null) {
      return null;
    }
    return new UserSpendingLimit(
        entity.getId(), entity.getServiceUserId(), copy(entity.getLimits()));
  }

  public UserSpendingLimitEntity toEntity(UserSpendingLimit limit) {
    if (limit == null) {
      return null;
    }
    return UserSpendingLimitEntity.builder()
        .id(limit.id())
        .serviceUserId(limit.serviceUserId())
        .limits(copy(limit.limits()))
        .build();
  }

  private Map<ConsumptionCategory, java.math.BigDecimal> copy(
      Map<ConsumptionCategory, java.math.BigDecimal> source) {
    if (source == null) {
      return new EnumMap<>(ConsumptionCategory.class);
    }
    return new EnumMap<>(source);
  }
}
