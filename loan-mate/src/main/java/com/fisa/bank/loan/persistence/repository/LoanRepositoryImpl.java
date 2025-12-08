package com.fisa.bank.loan.persistence.repository;

import com.fisa.bank.persistence.loan.enums.RepaymentStatus;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.persistence.loan.entity.LoanLedger;
import com.fisa.bank.persistence.loan.entity.id.LoanLedgerId;
import com.fisa.bank.persistence.loan.repository.LoanLedgerRepository;
import com.fisa.bank.persistence.user.entity.id.UserId;

@Repository
@RequiredArgsConstructor
public class LoanRepositoryImpl implements LoanRepository {
  private final LoanLedgerRepository loanLedgerRepository;

    @Override
    public List<Loan> getLoansNonTerminated(UserId userId) {
        List<LoanLedger> loanLedgers = loanLedgerRepository.findAllByUser_UserId(userId);


        return loanLedgers.stream()
                .filter(loanLedger -> loanLedger.getRepaymentStatus() == RepaymentStatus.NORMAL
                || loanLedger.getRepaymentStatus() == RepaymentStatus.OVERDUE)
                .map(LoanRepositoryImpl::toDomain)
                .toList();
    }

    @Override
  public List<Loan> getLoans(UserId userId) {
    // TODO: Persistence에서만 값 객체 쓰기, 서비스 서버 유저 id로 조회하기
    List<Loan> loans =
        loanLedgerRepository.findAllByUser_UserId(userId).stream()
            .map(LoanRepositoryImpl::toDomain)
            .toList();
    return loans;
  }

  public static Loan toDomain(LoanLedger loanLedger) {
    return new Loan(
        loanLedger.getLoanLedgerId().getValue(),
        loanLedger.getLoanProduct().getName(),
        loanLedger.getNextRepaymentDate(),
        loanLedger.isAutoDepositEnabled());
    //        loanLedger.getCreatedAt(),
    //        loanLedger.getLastRepaymentDate(),
    //        loanLedger.getTerm(),
    //        loanLedger.getRepaymentStatus());
  }

  @Override
  public Optional<Loan> findById(Long loanId) {
    return loanLedgerRepository.findById(LoanLedgerId.of(loanId)).map(LoanRepositoryImpl::toDomain);
  }
}
