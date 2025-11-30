package com.fisa.bank.account.application.service;

import com.fisa.bank.account.application.dto.request.TransferRequest;
import com.fisa.bank.account.application.dto.response.ApiResponse;
import com.fisa.bank.account.application.dto.response.SuccessBody;
import com.fisa.bank.account.application.dto.response.TransferResponse;
import com.fisa.bank.common.application.util.core_bank.CoreBankingClient;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fisa.bank.account.application.model.AccountDetail;
import com.fisa.bank.account.application.repository.AccountDetailRepository;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.persistence.user.entity.id.UserId;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AccountService {
  private final AccountDetailRepository accountDetailRepository;
  private final RequesterInfo requesterInfo;
  private final CoreBankingClient coreBankingClient;

  public List<AccountDetail> getAccounts() {
    Long userId = requesterInfo.getCoreBankingUserId();
    return accountDetailRepository.findAccountsByUserId(UserId.of(userId));
  }

  public TransferResponse transfer(TransferRequest request) {
    return coreBankingClient.post(
            "/api/accounts/transfer",
            request,
            TransferResponse.class
    );
  }
}
