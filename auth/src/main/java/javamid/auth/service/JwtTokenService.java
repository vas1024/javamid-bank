package javamid.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;


@Service
public class JwtTokenService {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes());
  }


  public String createToken(String username, Long userId) {
    return Jwts.builder()
            .setSubject(username)
            .claim("userId", userId)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith( SignatureAlgorithm.HS256, getSigningKey() )
            .compact();
  }

/*
  public String createToken(String username, Long userId) {
    return Jwts.builder()
            .setSubject(username)
            .claim("userId", userId)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS256, secret)  // Просто строка!
            .compact();
  }
*/

  public boolean isValid(String token) {
    try {
      Jwts.parserBuilder()
              .setSigningKey(getSigningKey())
              .build()
              .parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public String getUsername(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
  }

  public Long getUserId(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("userId", Long.class);
  }
}



/*

@Service
public class JwtTokenService {

  @Value("${jwt.secret:mySecretKey}") // Секрет из конфига
  private String secret;

  @Value("${jwt.expiration:3600000}") // 1 час по умолчанию
  private long expiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes());
  }


  public String createToken(String username) {
    return JWT.create()
            .withSubject(username)
            .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 час
            .sign(Algorithm.HMAC256(secret));
  }


  // Генерация токена
  public String generateToken(String username) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
  }

  // Извлечение username из токена
  public String getUsernameFromToken(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
  }

  // Валидация токена
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
              .setSigningKey(getSigningKey())
              .build()
              .parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  // Проверка срока действия
  public boolean isTokenExpired(String token) {
    Date expiration = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();
    return expiration.before(new Date());
  }
}

 */