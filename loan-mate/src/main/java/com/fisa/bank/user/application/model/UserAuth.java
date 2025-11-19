package com.fisa.bank.user.application.model;

import lombok.Getter;

@Getter
public class UserAuth {

  private final Long id;

  // 코어 뱅킹과 매핑
  private final Long coreBankingUserId; // 외부 시스템 ID
  private final Long serviceUserId; // 우리 서비스 User ID

  public UserAuth(Long id, Long coreBankingUserId, Long serviceUserId) {
    this.id = id;
    this.coreBankingUserId = coreBankingUserId;
    this.serviceUserId = serviceUserId;
  }

  public static UserAuth create(Long coreBankingUserId, Long serviceUserId) {
    return new UserAuth(null, coreBankingUserId, serviceUserId);
  }
}
