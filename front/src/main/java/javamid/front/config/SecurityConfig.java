package javamid.front.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javamid.front.model.UserAuthDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  // Инъекция через конструктор
  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
//            .addFilterBefore(new RequestDebugFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/signup", "/login", "/error", "/css/**", "/js/**", "/api/rates").permitAll()
                    .requestMatchers("/api/notifications/*").access(this::checkUserAccess)
                    .requestMatchers("/user/*/**").access(this::checkUserAccess)
                    .requestMatchers("/*").access(this::checkUserAccess)
                    .anyRequest().authenticated()
            )

            .formLogin(form -> form
                    .disable()
//                            .loginPage("/login")
//                            .permitAll()
            )
            .exceptionHandling(exception -> exception
                    // ✅ РЕДИРЕКТ НА ЛОГИН ПРИ ОТСУТСТВИИ АУТЕНТИФИКАЦИИ
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                    .accessDeniedHandler(new AccessDeniedHandler() { //  403 → login
                      @Override
                      public void handle(HttpServletRequest request, HttpServletResponse response,
                                         AccessDeniedException accessDeniedException) throws IOException {
                        response.sendRedirect("/login");
                      }
                    })
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .deleteCookies("jwt_token")    // ✅ Удаляем JWT cookie
                    .invalidateHttpSession(true)
                    .permitAll()
            )
            .csrf(csrf -> csrf.disable())      // ✅ Для JWT можно отключить
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // ✅ Без сессий
            )
            .build();
  }


  private AuthorizationDecision checkUserAccess(
          Supplier<Authentication> authenticationSupplier,
          RequestAuthorizationContext context) {

    String path = context.getRequest().getServletPath();
    System.out.println("=== ACCESS CHECK START ===");
    System.out.println("Path: " + path);
    System.out.println("All variables: " + context.getVariables());

    try {
      // Извлекаем userId из пути
//      String userIdFromPath = context.getVariables().get("userId");
//      String userIdFromPath = context.getVariables().get("userId");
      String userIdFromPath = extractIdFromPath(path);
      System.out.println("Extracted ID from variables: '" + userIdFromPath + "'");

      if (userIdFromPath == null || !userIdFromPath.matches("\\d+")) {
        System.out.println("❌ ID is null or not a number");
        return new AuthorizationDecision(false);
      }

      Long targetUserId = Long.parseLong(userIdFromPath);
      System.out.println("Target user ID: " + targetUserId);

      // Получаем аутентификацию
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      System.out.println("SecurityContext auth: " + authentication);
      System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));

      if (authentication == null || !authentication.isAuthenticated()) {
        return new AuthorizationDecision(false);
      }

      Object principal = authentication.getPrincipal();

      if (principal instanceof UserAuthDetails) {
        UserAuthDetails userDetails = (UserAuthDetails) principal;
        Long currentUserId = userDetails.getUserId();

        System.out.println("Current user ID: " + currentUserId);


        // Сравниваем userId из токена с userId из пути
        boolean accessGranted = currentUserId.equals(targetUserId);
        System.out.println("Access check: " + currentUserId + " == " + targetUserId + " → " + accessGranted);
        return new AuthorizationDecision(accessGranted);
      } else {
        System.out.println("❌ Principal is not UserAuthDetails: " + principal.getClass());
        System.out.println("Principal is not UserAuthDetails: " + (principal != null ? principal.getClass() : "null"));
      }

    } catch (Exception e) {
      System.out.println("Access check error: " + e.getMessage());
      e.printStackTrace();
    }

    System.out.println("=== ACCESS CHECK END ===");

    return new AuthorizationDecision(false);
  }






  private String extractIdFromPath(String path) {
    if (path == null || path.equals("/")) return null;

//    System.out.println("Extracting ID from path: " + path);

    // Для /47
    if (path.matches("/\\d+")) {
      return path.substring(1); // убираем первый слэш
    }

    // Для /api/notifications/47
    if (path.matches("/api/notifications/\\d+")) {
      String[] parts = path.split("/");
      return parts[parts.length - 1]; // последняя часть
    }

    // Для /user/47/cash и т.д.
    if (path.matches("/user/\\d+/.*")) {
      String[] parts = path.split("/");
      return parts[2]; // третья часть (индекс 2)
    }

    return null;
  }






}