package com.fisa.bank.account.presentation.scheduler;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fisa.bank.account.application.usecase.RunAutoDepositUseCase;

@Component
@RequiredArgsConstructor
public class AutoDepositScheduler {

  private final RunAutoDepositUseCase runAutoDepositUseCase;

  @Scheduled(cron = "0 0 3 * * *") // 매일 오전 3시에 자동예치
  public void runAutoDepositBatch() {
    runAutoDepositUseCase.run();
  }
}
