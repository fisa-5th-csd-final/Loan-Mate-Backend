package com.fisa.bank.account.presentation.controller;

import com.fisa.bank.account.application.dto.request.TransferRequest;
import com.fisa.bank.account.application.dto.response.TransferResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.fisa.bank.account.application.model.AccountDetail;
import com.fisa.bank.account.application.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
  private final AccountService accountService;

  @GetMapping
  public List<AccountDetail> getMyAccounts() {
    return accountService.getAccounts();
  }

  @PostMapping("/transfer")
  public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
    return accountService.transfer(request);
  }
}
