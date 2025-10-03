package javamid.exchange;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/rates").permitAll()           // 🔓 Public access
                    .requestMatchers("/api/bulk").hasAuthority("SCOPE_write") // 🔐 Protected
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwkSetUri("http://localhost:9000/oauth2/jwks"))
            )
            .csrf(csrf -> csrf.disable());

    return http.build();
  }
}