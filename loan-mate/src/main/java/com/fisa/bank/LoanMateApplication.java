package com.fisa.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.fisa.bank.infrastructure.cdc.CdcReplicationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CdcReplicationProperties.class)
public class LoanMateApplication {

  public static void main(String[] args) {
    SpringApplication.run(LoanMateApplication.class, args);
  }
}
