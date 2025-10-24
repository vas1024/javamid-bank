package javamid.exchange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /*
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder ) throws Exception {
    http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/rates").permitAll()           // 🔓 Public access
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers( "/api/bulk").hasAuthority("SCOPE_write") // 🔐 Protected
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
//                    .jwt(jwt -> jwt.jwkSetUri("http://localhost:9000/oauth2/jwks"))
                            .jwt( jwt -> jwt.decoder( jwtDecoder ) )
            )
            .csrf(csrf -> csrf.disable());

    return http.build();
  }
     */


// в одном filter chain почему-то не работает, пытается проверять токен всегда, и падает, когда transfer service присылает токен пользователя при чтении курсов

  // 🔓 Security chain для ПУБЛИЧНЫХ эндпоинтов (без OAuth2)
  @Bean
  @Order(1)
  public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
    return http
            .securityMatcher("/api/rates", "/actuator/**") // 🔽 ТОЛЬКО публичные пути
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll() // 🔽 ВСЕ разрешено без аутентификации
            )
            .csrf(csrf -> csrf.disable())
            .build();
  }

  // 🔐 Security chain для ЗАЩИЩЕННЫХ эндпоинтов (с OAuth2)
  @Bean
  @Order(2)
  public SecurityFilterChain protectedFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
    return http
            .securityMatcher("/api/bulk", "/api/rates/**") // 🔽 ТОЛЬКО защищенные пути
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.POST, "/api/rates").hasAuthority("SCOPE_write")
                    .requestMatchers("/api/bulk").hasAuthority("SCOPE_write")
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.decoder(jwtDecoder))
            )
            .csrf(csrf -> csrf.disable())
            .build();
  }





}