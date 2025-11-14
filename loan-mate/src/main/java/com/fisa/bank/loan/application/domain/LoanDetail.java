package com.fisa.bank.loan.application.domain;

import lombok.Getter;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.loan.application.util.JsonNodeUtils;
import com.fisa.bank.persistence.loan.enums.LoanType;
import com.fisa.bank.persistence.loan.enums.RepaymentType;

@Getter
public class LoanDetail {
  private BigDecimal remainPrincipal;
  private BigDecimal principal;
  private BigDecimal monthlyRepayment;
  private String accountNumber;
  private LoanType loanType;
  private RepaymentType repaymentType;

  public static LoanDetail from(JsonNode dataNode) {
    if (dataNode == null || dataNode.isMissingNode()) {
      return null;
    }

    LoanDetail info = new LoanDetail();

    info.remainPrincipal = JsonNodeUtils.getBigDecimal(dataNode, "remainPrincipal");
    info.principal = JsonNodeUtils.getBigDecimal(dataNode, "principal");
    info.monthlyRepayment = JsonNodeUtils.getBigDecimal(dataNode, "monthlyRepayment");

    info.accountNumber = dataNode.path("accountNumber").asText(null);

    info.loanType = JsonNodeUtils.getEnum(dataNode, "loanType", LoanType.class);
    info.repaymentType = JsonNodeUtils.getEnum(dataNode, "repaymentType", RepaymentType.class);

    return info;
  }
}
