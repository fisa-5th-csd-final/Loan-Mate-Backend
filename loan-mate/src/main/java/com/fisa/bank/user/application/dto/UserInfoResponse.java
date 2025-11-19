package com.fisa.bank.user.application.dto;

import com.fisa.bank.user.application.model.CreditRating;
import com.fisa.bank.user.application.model.CustomerLevel;

/**
 * 인증 서버의 /me 엔드포인트 응답 DTO
 *
 * @param userId 코어뱅킹 유저 ID (인증 서버에서 받은 ID)
 * @param name 사용자 이름
 * @param address 주소
 * @param job 직업
 * @param creditLevel 신용 등급
 * @param customerLevel 고객 등급
 */
public record UserInfoResponse(
    Long userId,
    String name,
    String address,
    // LocalDate birthday,
    // BigDecimal income,
    String job,
    CreditRating creditLevel,
    CustomerLevel customerLevel) {}
