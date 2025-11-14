package com.fisa.bank.loan.persistence.entity.id;

import com.fisa.bank.common.persistence.id.BaseIdJavaType;
import org.hibernate.type.descriptor.java.LongJavaType;
import org.hibernate.type.descriptor.jdbc.BigIntJdbcType;

public class LoanIdJavaType extends BaseIdJavaType<Long, LoanId>{
    public static final LoanIdJavaType INSTANCE = new LoanIdJavaType();

    public LoanIdJavaType(){
        super(LoanId.class, LoanId::of, LongJavaType.INSTANCE, BigIntJdbcType.INSTANCE);
    }
}