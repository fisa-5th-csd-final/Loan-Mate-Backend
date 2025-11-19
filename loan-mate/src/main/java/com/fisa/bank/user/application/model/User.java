package com.fisa.bank.user.application.model;

import lombok.Getter;

@Getter
public class User {

  private final Long userId;
  private String name;
  private String address;
  private String job;
  private CreditRating creditLevel;
  private CustomerLevel customerLevel;

  public User(
      Long userId,
      String name,
      String address,
      String job,
      CreditRating creditLevel,
      CustomerLevel customerLevel) {
    this.userId = userId;
    this.name = name;
    this.address = address;
    this.job = job;
    this.creditLevel = creditLevel;
    this.customerLevel = customerLevel;
  }

  /** 이름 / 주소 / 직업 업데이트 */
  public void updateProfile(String name, String address, String job) {
    this.name = name;
    this.address = address;
    this.job = job;
  }

  /** 생일 / 소득 업데이트 */
  /*
  public void updatePersonalData(LocalDate birthday, BigDecimal income) {
    this.birthday = birthday;
    this.income = income;
  } */

  /** 신용등급 변경 */
  public void updateCreditRating(CreditRating newRating) {
    this.creditLevel = newRating;
  }

  /** 고객등급 업그레이드 */
  public void upgradeCustomerLevel(CustomerLevel newLevel) {
    if (newLevel.ordinal() < this.customerLevel.ordinal()) {
      this.customerLevel = newLevel;
    }
  }
}
