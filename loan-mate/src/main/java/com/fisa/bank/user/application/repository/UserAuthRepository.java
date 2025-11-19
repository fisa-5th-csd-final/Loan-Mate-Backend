package com.fisa.bank.user.application.repository;

import java.util.Optional;

import com.fisa.bank.user.application.model.UserAuth;

public interface UserAuthRepository {

  Optional<UserAuth> findByCoreBankingUserId(Long coreBankingUserId);

  Optional<UserAuth> findByServiceUserId(Long serviceUserId);

  UserAuth save(UserAuth userAuth);
}
