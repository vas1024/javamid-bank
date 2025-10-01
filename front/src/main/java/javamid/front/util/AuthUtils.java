package javamid.front.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class AuthUtils {

  private final ObjectMapper objectMapper = new ObjectMapper();

  // 🔽 ИЗВЛЕЧЕНИЕ ТОКЕНА ИЗ ЗАПРОСА
  public String extractTokenFromRequest(HttpServletRequest request) {
    // 1. Проверяем Authorization header
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    // 2. Проверяем Cookie
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("jwt_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }

  // 🔽 СОЗДАНИЕ HEADERS С ТОКЕНОМ
  public HttpHeaders createAuthHeaders(HttpServletRequest request) {
    String token = extractTokenFromRequest(request);
    HttpHeaders headers = new HttpHeaders();
    if (token != null) {
      headers.set("Authorization", "Bearer " + token);
    }
    return headers;
  }

  // 🔽 СОЗДАНИЕ ENTITY С ТОКЕНОМ (для GET/DELETE)
  public HttpEntity<String> createAuthEntity(HttpServletRequest request) {
    return new HttpEntity<>(createAuthHeaders(request));
  }

  // 🔽 СОЗДАНИЕ ENTITY С ТЕЛОМ И ТОКЕНОМ (для POST/PUT)
  public <T> HttpEntity<T> createAuthEntityWithBody(T body, HttpServletRequest request) {
    return new HttpEntity<>(body, createAuthHeaders(request));
  }

  // 🔽 ИЗВЛЕЧЕНИЕ USER_ID ИЗ ТОКЕНА (опционально)
  public Long extractUserIdFromToken(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length == 3) {
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        JsonNode payload = objectMapper.readTree(payloadJson);
        return payload.get("userId").asLong();
      }
    } catch (Exception e) {
      System.out.println("Failed to extract userId from token: " + e.getMessage());
    }
    return null;
  }
}