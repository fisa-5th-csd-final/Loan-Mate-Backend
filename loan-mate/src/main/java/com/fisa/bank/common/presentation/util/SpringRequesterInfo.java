package com.fisa.bank.common.presentation.util;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.common.config.security.ServiceUserAuthentication;
import com.fisa.bank.user.application.model.UserAuth;
import com.fisa.bank.user.application.repository.UserAuthRepository;

@Component
@RequiredArgsConstructor
public class SpringRequesterInfo implements RequesterInfo {

  private final UserAuthRepository userAuthRepository;
  private final Map<Long, Long> serviceToCoreBankingCache = new ConcurrentHashMap<>();

  @Override
  public Long getServiceUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new IllegalStateException("SecurityContext authentication 없음");
    }

    if (!(authentication instanceof ServiceUserAuthentication serviceAuth)) {
      throw new IllegalStateException("SecurityContext에서 ServiceUserAuthentication 찾을 수 없음");
    }

    return serviceAuth.getUserId();
  }

  @Override
  public Long getCoreBankingUserId() {
    Long serviceUserId = getServiceUserId();

    return serviceToCoreBankingCache.computeIfAbsent(
        serviceUserId,
        id ->
            userAuthRepository
                .findByServiceUserId(id)
                .map(UserAuth::getCoreBankingUserId)
                .orElseThrow(() -> new IllegalStateException("코어뱅킹 userId 찾을 수 없음")));
  }
}
