package com.fisa.bank.loan.persistence.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fisa.bank.loan.application.domain.Loan;
import com.fisa.bank.loan.persistence.entity.id.LoanId;
import com.fisa.bank.loan.persistence.entity.id.LoanIdJavaType;

@Entity
@Table(name = "loan")
public class LoanEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JavaType(LoanIdJavaType.class)
  @JdbcTypeCode(SqlTypes.BIGINT)
  private LoanId loanId;

  // TODO: 나중에 userId로 바꿔야 함
  private Long userId;

  // 가입한 대출 이름
  private String loanName;

  public Loan toDomain() {
    return new Loan(loanName);
  }
}
