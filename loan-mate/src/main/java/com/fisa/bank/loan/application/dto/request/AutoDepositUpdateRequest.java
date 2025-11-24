package com.fisa.bank.loan.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoDepositUpdateRequest {
  private boolean autoDepositEnabled;
}
