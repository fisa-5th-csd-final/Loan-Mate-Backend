package com.fisa.bank.account.application.model;

import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.user.entity.User;
import com.fisa.bank.user.application.model.ServiceUser;

public record UserAccountContext(ServiceUser serviceUser, User coreUser, Account salaryAccount) {}
