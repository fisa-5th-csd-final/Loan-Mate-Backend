package com.fisa.bank.user.application.usecase;

import com.fisa.bank.user.application.dto.UserInfoResponse;
import com.fisa.bank.user.application.service.LoginResult;

public interface SyncCoreBankUserUseCase {
  LoginResult sync(UserInfoResponse info);
}
