package javamid.auth.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.source.JWKSource;


@Configuration
public class SecurityConfig {

   @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      return http
              .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // Всех пускаем!
              .csrf(csrf -> csrf.disable())
              .build();
    }
}


  /*

  // Конфигурация для OAuth2 endpoints
  @Bean
  @Order(1)
  public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

    return http
            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
            )
            .formLogin(Customizer.withDefaults())
            .build();
  }

  // Конфигурация для обычных запросов
  @Bean
  @Order(2)
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(authorize -> authorize
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                    .loginPage("/login")
                    .permitAll()
            )
            .build();
  }

  // Сервис пользователей
  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails user = User.withUsername("user")
            .password("{noop}password")
            .roles("USER")
            .build();

    return new InMemoryUserDetailsManager(user);
  }

  // Bean для работы с JWT
  @Bean
  public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
  }
}


   */