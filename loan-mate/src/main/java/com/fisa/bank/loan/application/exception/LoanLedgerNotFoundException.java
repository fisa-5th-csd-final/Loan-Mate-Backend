package com.fisa.bank.loan.application.exception;

public class LoanLedgerNotFoundException extends RuntimeException {
  public LoanLedgerNotFoundException(Long id) {
    super("LoanLedger not found. id=" + id);
  }
}
