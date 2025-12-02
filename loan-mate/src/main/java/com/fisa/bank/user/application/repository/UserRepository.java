package com.fisa.bank.user.application.repository;

import java.util.Optional;

import com.fisa.bank.user.application.model.ServiceUser;

public interface UserRepository {

  Optional<ServiceUser> findById(Long userId);

  ServiceUser save(ServiceUser user);
}
