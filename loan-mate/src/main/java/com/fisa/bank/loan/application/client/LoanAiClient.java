package com.fisa.bank.loan.application.client;

import lombok.RequiredArgsConstructor;

import java.util.Map;

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

  public LoanRisks fetchLoanRisks(Long userId) {
    return aiClient.fetchOne(PREDICT_URL, Map.of(AI_REQUEST_KEY_USER_ID, userId), LoanRisks.class);
  }

  public LoanComment fetchLoanComment(Long loanLedgerId) {
    return aiClient.fetchOne(
        LOAN_COMMENT_URL, Map.of(AI_REQUEST_KEY_LOAN_LEDGER_ID, loanLedgerId), LoanComment.class);
  }
}
