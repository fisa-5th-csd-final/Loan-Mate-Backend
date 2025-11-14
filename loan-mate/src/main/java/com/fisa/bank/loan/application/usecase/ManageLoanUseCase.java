package com.fisa.bank.loan.application.usecase;

import java.util.List;

import com.fisa.bank.loan.application.domain.LoanDetail;
import com.fisa.bank.loan.application.dto.response.LoanListResponse;

public interface ManageLoanUseCase {

  List<LoanListResponse> getLoans(Long userId);

    LoanDetail getLoanDetail(Long loanId);

}
