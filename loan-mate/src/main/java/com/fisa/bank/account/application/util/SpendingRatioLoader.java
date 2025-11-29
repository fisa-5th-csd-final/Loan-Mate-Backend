package com.fisa.bank.account.application.util;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;
import com.fisa.bank.user.application.model.User;

@Component
@RequiredArgsConstructor
@Getter
public class SpendingRatioLoader {

  private final ObjectMapper objectMapper;

  private Map<String, Map<ConsumptionCategory, BigDecimal>> ratios;

  @PostConstruct
  public void load() throws IOException {
    ClassPathResource resource = new ClassPathResource("spending-ratios.json");

    TypeReference<Map<String, Map<ConsumptionCategory, BigDecimal>>> typeRef =
        new TypeReference<>() {};

    ratios = objectMapper.readValue(resource.getInputStream(), typeRef);
  }

  public Map<ConsumptionCategory, BigDecimal> getRatios(String ageGroup) {
    return ratios.get(ageGroup);
  }

  private int calculateAge(User user) {
    return java.time.Period.between(user.getBirthday(), java.time.LocalDate.now()).getYears();
  }

  private String resolveAgeGroup(int age) {
    if (age < 30) return "T20";
    else if (age < 40) return "T30";
    else if (age < 50) return "T40";
    else if (age < 60) return "T50";
    else if (age < 70) return "T60";
    else if (age < 80) return "T70";
    else if (age < 90) return "T80";
    else return "T90";
  }
}
