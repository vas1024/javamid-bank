package javamid.auth.controller;

import javamid.auth.client.AccountsClient;
import javamid.auth.model.*;
import javamid.auth.service.JwtTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthController {

  private final JwtTokenService jwtTokenService;
  private final AccountsClient accountsClient;
  public AuthController( JwtTokenService jwtTokenService,
                         AccountsClient accountsClient
  ){
    this.jwtTokenService = jwtTokenService;
    this.accountsClient = accountsClient;
  }


  // 1. Endpoint для логина (вызывается один раз)
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
    // Проверяем логин/пароль в accounts service
    Long userId = accountsClient.checkLogin(request);
    if (userId > 0) {
      // Генерируем новый токен
      String token = jwtTokenService.createToken(request.getUsername(), userId );
      return ResponseEntity.ok(new AuthResponse(true, token, "Bearer", 3600L, userId));
    }
    return ResponseEntity.status(401).body(new AuthResponse(false, null, null, null, null));
  }


  // 2. Endpoint для валидации (вызывается при каждом запросе)
  @PostMapping("/validate")
  public boolean validate(@RequestBody String token) {
    System.out.println( "Controller: validating token: " + token );
    boolean isValid = jwtTokenService.isValid(token);
    return isValid;
  }
}