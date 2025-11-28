package com.fisa.bank.user.application.client;

import org.springframework.stereotype.Component;

import com.fisa.bank.common.application.util.core_bank.CoreBankingClient;
import com.fisa.bank.user.application.dto.UserInfoResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CoreBankingUserClient {

  private final CoreBankingClient coreBankingClient;

  public UserInfoResponse fetchMe() {
    return coreBankingClient.fetchOne("/users/me", UserInfoResponse.class);
  }
}
