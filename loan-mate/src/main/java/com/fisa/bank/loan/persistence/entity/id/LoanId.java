package com.fisa.bank.loan.persistence.entity.id;

import com.fisa.bank.common.persistence.id.BaseId;

public class LoanId extends BaseId<Long> {
    private LoanId(Long value){ super(value); }

    public static LoanId of(Long value){ return new LoanId(value); };
}

