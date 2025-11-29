package com.fisa.bank.account.presentation.controller;

import com.fisa.bank.account.application.model.AccountDetail;
import com.fisa.bank.account.application.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/{accountId}")
    public AccountDetail getAccount(@PathVariable Long accountId) {
        return accountService.getAccount(accountId);
    }
}
