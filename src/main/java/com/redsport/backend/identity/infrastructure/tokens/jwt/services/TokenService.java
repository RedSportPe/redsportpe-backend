package com.redsport.backend.identity.infrastructure.tokens.jwt.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration-days}")
  private int expirationDays;

  @jakarta.annotation.PostConstruct
  void validateConfiguration() {
    getSigningKey();
    if (expirationDays < 1) throw new IllegalArgumentException("JWT expiration must be positive");
  }

  /** Generates a signed JWT carrying the user's email (subject) and role. */
  public String generateToken(String email, String role) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + (long) expirationDays * 24 * 60 * 60 * 1000);

    return Jwts.builder()
        .subject(email)
        .claim("role", role)
        .issuedAt(now)
        .expiration(expiration)
        .signWith(getSigningKey())
        .compact();
  }

  public String getEmailFromToken(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private <T> T extractClaim(String token, Function<Claims, T> resolver) {
    Claims claims =
        Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    return resolver.apply(claims);
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
