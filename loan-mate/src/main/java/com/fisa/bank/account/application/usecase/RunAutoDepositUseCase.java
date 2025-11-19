package com.fisa.bank.account.application.usecase;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fisa.bank.account.application.service.AutoDepositService;
import com.fisa.bank.account.persistence.repository.LoanLedgerRepository;
import com.fisa.bank.persistence.loan.entity.LoanLedger;

// 스케줄러에서 호출하는 공용 API 역할
@Component
@RequiredArgsConstructor
public class RunAutoDepositUseCase {

  private final LoanLedgerRepository loanLedgerRepository;
  private final AutoDepositService autoDepositService;

  public void run() {
    LocalDate today = LocalDate.now();

    List<LoanLedger> targets =
        loanLedgerRepository.findByAutoDepositEnabledTrueAndNextRepaymentDate(today);

    targets.forEach(autoDepositService::execute);
  }
}
