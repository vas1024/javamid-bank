package javamid.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuthDebugController {

  @GetMapping("/oauth-debug")
  public String oauthDebug() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return "Auth: " + (auth != null ? auth.getName() : "null");
  }
}