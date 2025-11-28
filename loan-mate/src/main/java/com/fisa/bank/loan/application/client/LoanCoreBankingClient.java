package com.fisa.bank.loan.application.client;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.common.application.util.core_bank.CoreBankingClient;
import com.fisa.bank.loan.application.dto.request.AutoDepositUpdateRequest;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.model.PrepaymentInfo;

@Component
@RequiredArgsConstructor
public class LoanCoreBankingClient {

  private static final String LOANS_BASE_PATH = "/loans";

  private final CoreBankingClient coreBankingClient;

  // LoanDetail 세부 정보 캐싱
  @Cacheable(
      cacheNames = "loanDetail",
      key = "@springRequesterInfo.coreBankingUserId + ':' + #loanId")
  public LoanDetail fetchLoanDetail(Long loanId) {
    return coreBankingClient.fetchOne(LOANS_BASE_PATH + "/ledger/" + loanId, LoanDetail.class);
  }

  // 조기 상환 정보 캐싱
  @Cacheable(cacheNames = "prePaymentInfo", key = "@springRequesterInfo.coreBankingUserId")
  public List<PrepaymentInfo> fetchPrepaymentInfos() {
    return coreBankingClient.fetchList(LOANS_BASE_PATH + "/prepayment-infos", PrepaymentInfo.class);
  }

  // 조기 상환이므로, 거의 모든 캐시 무효화
  // 조기 상환에 실패하면?
  @Caching(
      evict = {
        @CacheEvict(
            cacheNames = "loanDetail",
            key = "@springRequesterInfo.coreBankingUserId + ':' + #loanId"),
        @CacheEvict(cacheNames = "prePaymentInfo", key = "@springRequesterInfo.coreBankingUserId"),
        @CacheEvict(cacheNames = "loanComment", key = "#loanId"),
        @CacheEvict(cacheNames = "loanRisks", key = "@springRequesterInfo.coreBankingUserId")
      })
  public void cancelLoan(Long loanId) {
    coreBankingClient.delete(LOANS_BASE_PATH + "/" + loanId);
  }

  // 자동 예치 정보를 업데이트하므로 cache 무효화
  @Caching(
      evict = {
        @CacheEvict(
            cacheNames = "loanDetail",
            key = "@springRequesterInfo.coreBankingUserId + ':' + #loanId"),
        @CacheEvict(cacheNames = "prePaymentInfo", key = "@springRequesterInfo.coreBankingUserId"),
      })
  public void updateAutoDeposit(Long loanId, boolean autoDepositEnabled) {
    coreBankingClient.patch(
        LOANS_BASE_PATH + "/" + loanId + "/auto-deposit",
        new AutoDepositUpdateRequest(autoDepositEnabled));
  }
}
