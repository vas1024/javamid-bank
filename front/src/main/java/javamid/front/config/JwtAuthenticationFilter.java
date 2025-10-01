package javamid.front.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javamid.front.model.UserAuthDetails;
import javamid.front.model.ValidationResponse;
import javamid.front.model.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;



@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final RestTemplate restTemplate;

  public JwtAuthenticationFilter(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws IOException, ServletException {

    String token = extractTokenFromRequest(request);

    System.out.println("=== JWT FILTER START ===");
    System.out.println("Request: " + request.getMethod() + " " + request.getServletPath());
    System.out.println("Token found: " + (token != null ? "YES" : "NO"));

    if (token != null) {
      try {
        // ✅ ВАЛИДАЦИЯ через REST вызов к Auth Service
        System.out.println("Validating token with auth service...");
        boolean isValid = validateTokenWithAuthService(token);
        System.out.println("Token valid: " + isValid);

        if (isValid) {
          // ✅ Получаем данные пользователя из токена (без валидации на FrontUI)
          String username = extractUsernameFromToken(token);
          Long userId = extractUserIdFromToken(token);

          System.out.println("JwtAthernticatonFilter : " + username + " " +userId );
          System.out.println("User: " + username + ", ID: " + userId);

          // Создаем объект аутентификации для Spring Security
          UserAuthDetails userDetails = new UserAuthDetails(userId, username);
          UsernamePasswordAuthenticationToken authentication =
                  new UsernamePasswordAuthenticationToken(
                          userDetails,
                          null,
                          userDetails.getAuthorities());

          // Добавляем userId в details для использования в контроллерах
          authentication.setDetails(new UserAuthDetails(userId, username));

          SecurityContextHolder.getContext().setAuthentication(authentication);
          System.out.println("Authentication set with UserAuthDetails principal");

        }
      } catch (Exception e) {
        // Логируем ошибку, но пропускаем запрос дальше (анонимный доступ)
        logger.warn("JWT validation failed: " + e.getMessage());
        System.out.println("JWT Filter ERROR: " + e.getMessage());
        SecurityContextHolder.clearContext();
      }
    }  else {
      System.out.println("No token, clearing context");
      SecurityContextHolder.clearContext();
    }

    System.out.println("=== JWT FILTER END ===");

    chain.doFilter(request, response);
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    // 1. Проверяем Cookie
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("jwt_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    // 2. Проверяем Authorization header
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    return null;
  }

  private boolean validateTokenWithAuthService(String token) {
    boolean isValid = false;
    try {
      // REST вызов к Auth Service для валидации
       isValid = restTemplate.postForObject(
               "http://gateway/auth/api/validate",
              token,
              Boolean.class
      );

      return isValid ;

    } catch (Exception e) {
      logger.error("Auth service unavailable: " + e.getMessage());
      return false;
    }
  }

  // 🔓 Простое извлечение данных из JWT БЕЗ проверки подписи
  // (Доверяем данным, так как токен уже валидирован через Auth Service)
  private String extractUsernameFromToken(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length == 3) {
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode payload = mapper.readTree(payloadJson);
        return payload.get("sub").asText(); // JWT subject = username
      }
    } catch (Exception e) {
      logger.warn("Failed to extract username from token: " + e.getMessage());
    }
    return null;
  }

  private Long extractUserIdFromToken(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length == 3) {
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode payload = mapper.readTree(payloadJson);
        return payload.get("userId").asLong(); // Кастомное поле
      }
    } catch (Exception e) {
      logger.warn("Failed to extract userId from token: " + e.getMessage());
    }
    return null;
  }

  /*
  private Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }*/

}