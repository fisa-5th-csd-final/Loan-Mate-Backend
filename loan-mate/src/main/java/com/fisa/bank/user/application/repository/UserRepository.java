package com.fisa.bank.user.application.repository;

import java.util.Optional;

import com.fisa.bank.user.application.model.User;

public interface UserRepository {

  Optional<User> findById(Long userId);

  User save(User user);
}
