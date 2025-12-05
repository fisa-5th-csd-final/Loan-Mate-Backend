package com.fisa.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fisa.bank.calculator.CalculatorService;

@Configuration
public class CalculatorConfig {

  @Bean
  public CalculatorService calculatorService() {
    return new CalculatorService();
  }
}
