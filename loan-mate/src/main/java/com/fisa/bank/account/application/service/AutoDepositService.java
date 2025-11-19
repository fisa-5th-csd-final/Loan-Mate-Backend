package com.fisa.bank.account.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.fisa.bank.account.persistence.entity.AutoDepositHistory;
import com.fisa.bank.account.persistence.repository.JpaAutoDepositHistoryRepository;
import com.fisa.bank.common.application.dto.LoanLedgerDetailResponse;
import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.persistence.loan.entity.LoanLedger;
import com.fisa.bank.persistence.loan.repository.LoanLedgerRepository;

@Service
@RequiredArgsConstructor
public class AutoDepositService {

  private final CoreBankingClient coreBankingClient;
  private final LoanLedgerRepository loanLedgerRepository;
  private final JpaAutoDepositHistoryRepository historyRepository;

  @Transactional
  public void execute(LoanLedger ledger) {

    // 1. 코어뱅킹에서 상세조회하여 monthlyRepayment 가져옴
    LoanLedgerDetailResponse detail =
        coreBankingClient.getLoanLedgerDetail(ledger.getLoanLedgerId().getValue());

    BigDecimal required = detail.getMonthlyRepayment(); // ← 이번달 자동예치 금액
    String accountNumber = detail.getAccountNumber();

    try {
      // 2. 계좌 잔액 조회
      BigDecimal balance = coreBankingClient.getBalance(accountNumber);

      if (balance.compareTo(required) < 0) {

        historyRepository.save(
            AutoDepositHistory.builder()
                .loanLedgerId(ledger.getLoanLedgerId().getValue())
                .status("FAILED")
                .message("연체: 잔액 부족 자동예치 실패")
                .executedAt(LocalDateTime.now())
                .build());

        return;
      }

      // 3. 출금
      coreBankingClient.withdraw(accountNumber, required);

      // 4. 성공 기록
      historyRepository.save(
          AutoDepositHistory.builder()
              .loanLedgerId(ledger.getLoanLedgerId().getValue())
              .amount(required)
              .status("SUCCESS")
              .executedAt(LocalDateTime.now())
              .build());

    } catch (Exception e) {

      historyRepository.save(
          AutoDepositHistory.builder()
              .loanLedgerId(ledger.getLoanLedgerId().getValue())
              .status("FAILED")
              .message(e.getMessage())
              .executedAt(LocalDateTime.now())
              .build());
    }
  }
}
