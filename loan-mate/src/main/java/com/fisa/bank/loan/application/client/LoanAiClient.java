package com.fisa.bank.loan.application.client;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fisa.bank.common.application.util.ai.AiClient;
import com.fisa.bank.loan.application.model.LoanComment;
import com.fisa.bank.loan.application.model.LoanRisks;

@Component
@RequiredArgsConstructor
public class LoanAiClient {
  private static final String PREDICT_URL = "/predict";
  private static final String LOAN_COMMENT_URL = "/insight/loan";

  private static final String AI_REQUEST_KEY_USER_ID = "user_id";
  private static final String AI_REQUEST_KEY_LOAN_LEDGER_ID = "loan_ledger_id";

  private final AiClient aiClient;

  // 사용자의 전체 대출 Ledger를 종합적으로 고려한 Risk
  @Cacheable(cacheNames = "loanRisk", key = "#userId")
  public LoanRisks fetchLoanRisks(Long userId) {
    return aiClient.fetchOne(PREDICT_URL, Map.of(AI_REQUEST_KEY_USER_ID, userId), LoanRisks.class);
  }

  // 사용자 1명이 가입한 1건의 대출 Ledger에 대한 comment
  @Cacheable(cacheNames = "loanComment", key = "@springRequesterInfo.coreBankingUserId + ':' + #loanLedgerId")
  public LoanComment fetchLoanComment(Long loanLedgerId) {
    return aiClient.fetchOne(
        LOAN_COMMENT_URL, Map.of(AI_REQUEST_KEY_LOAN_LEDGER_ID, loanLedgerId), LoanComment.class);
  }
}
