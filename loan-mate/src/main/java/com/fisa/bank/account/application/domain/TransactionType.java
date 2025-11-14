package com.fisa.bank.account.application.domain;

public enum TransactionType {
  ATM_DEPOSIT, // ATM 입금
  ATM_WITHDRAW, // ATM 출금
  TRANSFER_SEND, // 송금 (보내는 쪽)
  TRANSFER_RECEIVE, // 송금 (받는 쪽)
  EXTERNAL_TRANSFER_SEND, // 타행 송금 (보내는 쪽)
  CARD_PAYMENT, // 카드 결제
  CARD_REFUND, // 카드 환불
  LOAN_REPAYMENT, // 대출 상환
  EARLY_REPAYMENT // 중도 상환
}
