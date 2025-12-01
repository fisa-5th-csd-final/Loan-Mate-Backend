package com.fisa.bank.account.application.service.spending;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.account.application.model.spending.UserSpendingLimit;
import com.fisa.bank.account.application.repository.UserSpendingLimitRepository;
import com.fisa.bank.account.application.usecase.GetUserSpendingLimitUseCase;
import com.fisa.bank.account.application.usecase.SaveUserSpendingLimitUseCase;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSpendingLimitService
    implements SaveUserSpendingLimitUseCase, GetUserSpendingLimitUseCase {

  private final UserSpendingLimitRepository userSpendingLimitRepository;
  private final RequesterInfo requesterInfo;

  @Override
  public UserSpendingLimit execute(Map<ConsumptionCategory, BigDecimal> limits) {
    Long serviceUserId = requesterInfo.getServiceUserId();

    Map<ConsumptionCategory, BigDecimal> normalized = normalize(limits);

    UserSpendingLimit toSave =
        userSpendingLimitRepository
            .findByServiceUserId(serviceUserId)
            .map(existing -> new UserSpendingLimit(existing.id(), serviceUserId, normalized))
            .orElseGet(() -> new UserSpendingLimit(null, serviceUserId, normalized));

    return userSpendingLimitRepository.save(toSave);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserSpendingLimit> execute() {
    Long serviceUserId = requesterInfo.getServiceUserId();
    return userSpendingLimitRepository.findByServiceUserId(serviceUserId);
  }

  @Transactional(readOnly = true)
  public Map<ConsumptionCategory, BigDecimal> getLimitsOrDefault() {
    return normalize(
        execute()
            .map(UserSpendingLimit::limits)
            .orElseGet(() -> new EnumMap<>(ConsumptionCategory.class)));
  }

  private Map<ConsumptionCategory, BigDecimal> normalize(
      Map<ConsumptionCategory, BigDecimal> limits) {
    Map<ConsumptionCategory, BigDecimal> result = new EnumMap<>(ConsumptionCategory.class);
    if (limits == null) {
      return result;
    }
    limits.forEach(
        (category, value) -> {
          if (category != null && value != null) {
            result.put(category, value.max(BigDecimal.ZERO));
          }
        });
    return result;
  }
}
