package com.fisa.bank.account.presentation.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
