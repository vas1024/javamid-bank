package javamid.exchange.config;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import java.util.List;

@Configuration
public class JwtConfig {

  private final DiscoveryClient discoveryClient;

  public JwtConfig(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    String jwksUrl = getJwksUrlFromEureka();
    System.out.println("🔐 Using JWKS URL: " + jwksUrl);
    return NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();
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
