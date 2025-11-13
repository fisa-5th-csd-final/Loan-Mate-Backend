package com.fisa.bank.loan.persistence.entity;

import com.fisa.bank.common.persistence.entity.BaseEntity;
import com.fisa.bank.loan.application.domain.Loan;
import com.fisa.bank.loan.persistence.entity.id.LoanId;
import com.fisa.bank.loan.persistence.entity.id.LoanIdJavaType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class LoanEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JavaType(LoanIdJavaType.class)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private LoanId loanId;

    // TODO: 나중에 userId로 바꿔야 함
    private Long userId;

    // 가입한 대출 이름
    private String name;

    public Loan toDomain() {
        return new Loan(name);
    }
}
