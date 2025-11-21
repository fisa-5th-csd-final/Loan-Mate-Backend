package com.fisa.bank.user.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RefreshTokenEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private Long userId;

  @Column(nullable = false, unique = true, length = 500)
  private String token;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private Instant createdAt;

  public static RefreshTokenEntity create(Long userId, String token, Instant expiresAt) {
    return RefreshTokenEntity.builder()
        .userId(userId)
        .token(token)
        .expiresAt(expiresAt)
        .createdAt(Instant.now())
        .build();
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public void updateToken(String newToken, Instant newExpiresAt) {
    this.token = newToken;
    this.expiresAt = newExpiresAt;
  }
}
