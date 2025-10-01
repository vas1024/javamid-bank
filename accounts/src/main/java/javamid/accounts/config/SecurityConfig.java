package javamid.accounts.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public SecurityConfig( RestTemplate restTemplate ) { this.restTemplate = restTemplate; }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
            .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth

                    .requestMatchers("/api/users/login").permitAll()
                            .requestMatchers("/api/users").permitAll() // signup

                    // 🔐 Проверка доступа для user-specific endpoints
                    .requestMatchers("/api/users/{userId}/**").access(this::checkUserAccess)
                    .requestMatchers("/api/users/{userId}").access(this::checkUserAccess)

                    // 🔐 Все остальные API endpoints требуют аутентификации
                    .requestMatchers("/api/**").authenticated()

                    // 🔓 Публичные endpoints (если есть)
                    .requestMatchers("/health", "/error").permitAll()

                    .anyRequest().authenticated()

//                            .anyRequest().permitAll()

            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .build();
  }

  // 🔐 Проверка что userId из токена совпадает с userId в пути
  private AuthorizationDecision checkUserAccess(
          Supplier<Authentication> authenticationSupplier,
          RequestAuthorizationContext context) {

    try {
      // Извлекаем userId из пути
      String userIdFromPath = context.getVariables().get("userId");
      if (userIdFromPath == null || !userIdFromPath.matches("\\d+")) {
        return new AuthorizationDecision(false);
      }

      Long targetUserId = Long.parseLong(userIdFromPath);

      // Получаем аутентификацию
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null || !authentication.isAuthenticated()) {
        return new AuthorizationDecision(false);
      }

      // Извлекаем userId из аутентификации
      Object principal = authentication.getPrincipal();
      if (principal instanceof UserAuthDetails) {
        UserAuthDetails userDetails = (UserAuthDetails) principal;
        Long currentUserId = userDetails.getUserId();

        // Сравниваем userId из токена с userId из пути
        boolean accessGranted = currentUserId.equals(targetUserId);
        return new AuthorizationDecision(accessGranted);
      }

    } catch (Exception e) {
      // Логируем ошибку
      System.out.println("Access check error: " + e.getMessage());
    }

    return new AuthorizationDecision(false);
  }

  // 🔐 JWT Filter как внутренний класс
  private class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

      System.out.println("=== ACCOUNTS JWT FILTER START ===");
      System.out.println("Request to Accounts: " + request.getMethod() + " " + request.getServletPath());

      String token = extractTokenFromRequest(request);

      System.out.println("Token found in Accounts: " + (token != null ? "YES" : "NO"));

      if (token != null) {
        try {
          // Валидируем токен через Auth Service
          boolean isValid = validateTokenWithAuthService(token);

          System.out.println("Token validation result: " + isValid);

          if (isValid) {
            // Извлекаем данные из токена
            String username = extractUsernameFromToken(token);
            Long userId = extractUserIdFromToken(token);

            System.out.println("Accounts - User authenticated: " + username + ", ID: " + userId);

            // Создаем аутентификацию для Spring Security
            UserAuthDetails userDetails = new UserAuthDetails(userId, username);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("SecurityContext set in Accounts service");

          }
        } catch (Exception e) {
          // Логируем ошибку, но пропускаем запрос дальше
          System.out.println("JWT validation failed: " + e.getMessage());
        }
      }

      chain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
      // 1. Проверяем Authorization header
      String authHeader = request.getHeader("Authorization");

      System.out.println("Authorization header in Accounts: '" + authHeader + "'");

      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        return authHeader.substring(7);
      }

      // 2. Проверяем Cookie (если нужно)
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

    private boolean validateTokenWithAuthService(String token) {
      try {
        Boolean isValid = restTemplate.postForObject(
                "http://gateway/auth/api/validate",
                token,
                Boolean.class
        );
        return isValid != null && isValid;
      } catch (Exception e) {
        System.out.println("Auth service unavailable: " + e.getMessage());
        return false;
      }
    }

    private String extractUsernameFromToken(String token) {
      try {
        String[] parts = token.split("\\.");
        if (parts.length == 3) {
          String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
          JsonNode payload = objectMapper.readTree(payloadJson);
          return payload.get("sub").asText(); // JWT subject = username
        }
      } catch (Exception e) {
        System.out.println("Failed to extract username from token: " + e.getMessage());
      }
      return null;
    }

    private Long extractUserIdFromToken(String token) {
      try {
        String[] parts = token.split("\\.");
        if (parts.length == 3) {
          String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
          JsonNode payload = objectMapper.readTree(payloadJson);
          return payload.get("userId").asLong(); // Кастомное поле
        }
      } catch (Exception e) {
        System.out.println("Failed to extract userId from token: " + e.getMessage());
      }
      return null;
    }
  }

  // 🔐 UserDetails implementation
  public static class UserAuthDetails {
    private final Long userId;
    private final String username;

    public UserAuthDetails(Long userId, String username) {
      this.userId = userId;
      this.username = username;
    }

    public Long getUserId() {
      return userId;
    }

    public String getUsername() {
      return username;
    }

    public Collection<SimpleGrantedAuthority> getAuthorities() {
      return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
  }


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


}