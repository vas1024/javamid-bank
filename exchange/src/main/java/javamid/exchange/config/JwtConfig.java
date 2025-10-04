package javamid.exchange.config;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import java.util.List;

@Configuration
public class JwtConfig {

  private final DiscoveryClient discoveryClient;

  public JwtConfig(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  /*
  @Bean
  public JwtDecoder jwtDecoder() {
    String jwksUrl = getJwksUrlFromEureka();
    System.out.println("🔐 Using JWKS URL: " + jwksUrl);
    return NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
  }*/

  @Bean
  public JwtDecoder jwtDecoder() {
    String jwksUrl = getJwksUrlFromEureka();
    System.out.println("🔐 Using JWKS URL: " + jwksUrl);

    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();

    // Добавляем логирование каждого декодированного токена
    return token -> {
      System.out.println("=== EXCHANGE SERVICE JWT DECODING ===");
      System.out.println("🔐 Received JWT token: " + token);

      try {
        Jwt jwt = jwtDecoder.decode(token);

        System.out.println("📄 JWT Details:");
        System.out.println("   👤 Subject: " + jwt.getSubject());
        System.out.println("   📋 Scopes: " + jwt.getClaimAsStringList("scope"));
        System.out.println("   🆔 JWT ID: " + jwt.getId());
        System.out.println("   🏢 Issuer: " + jwt.getIssuer());
        System.out.println("   ⏰ Expires: " + jwt.getExpiresAt());
        System.out.println("   🎯 Audience: " + jwt.getAudience());
        System.out.println("   📝 Claims: " + jwt.getClaims());

        // Проверяем наличие scope 'write'
        List<String> scopes = jwt.getClaimAsStringList("scope");
        boolean hasWriteScope = scopes != null && scopes.contains("write");
        System.out.println("   ✅ Has 'write' scope: " + hasWriteScope);

        System.out.println("=== END JWT DECODING ===");

        return jwt;

      } catch (Exception e) {
        System.out.println("❌ JWT Decoding failed: " + e.getMessage());
        System.out.println("=== END JWT DECODING ===");
        throw e;
      }
    };
  }



  private String getJwksUrlFromEureka() {
    try {
      // Ищем сервис "auth" в Eureka
      List<org.springframework.cloud.client.ServiceInstance> instances = discoveryClient.getInstances("auth");

      if (instances != null && !instances.isEmpty()) {
        // Берем первый доступный инстанс
        org.springframework.cloud.client.ServiceInstance instance = instances.get(0);
        String url = instance.getUri() + "/oauth2/jwks";
        System.out.println("✅ Found auth service in Eureka: " + url);
        return url;
      } else {
        System.out.println("⚠️ Auth service not found in Eureka, using fallback");
      }
    } catch (Exception e) {
      System.out.println("⚠️ Error discovering auth service: " + e.getMessage());
    }

    // Fallback на localhost если Eureka недоступна
    return "http://localhost:9000/oauth2/jwks";
  }
}
