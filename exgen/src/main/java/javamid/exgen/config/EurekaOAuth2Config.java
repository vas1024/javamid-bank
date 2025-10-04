package javamid.exgen.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import java.util.List;

@Configuration
public class EurekaOAuth2Config {

  @Bean
  @ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.exgen")
  public ClientRegistration.Builder clientRegistrationBuilder(DiscoveryClient discoveryClient) {
    String tokenUri = getTokenUriFromEureka(discoveryClient);
    return ClientRegistration.withRegistrationId("exgen")
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .tokenUri(tokenUri);
  }

  /*
  @Bean
  public ClientRegistrationRepository clientRegistrationRepository(ClientRegistration.Builder builder) {
    ClientRegistration registration = builder.build();
    System.out.println("🔐 OAuth2 configured with token URI: " + registration.getProviderDetails().getTokenUri());
    return new InMemoryClientRegistrationRepository(registration);
  }
  */



  @Bean
  public ClientRegistrationRepository clientRegistrationRepository(
          DiscoveryClient discoveryClient,
          Environment environment) {

    String tokenUri = getTokenUriFromEureka(discoveryClient);

    // Получаем свойства напрямую из Environment
    String clientId = environment.getProperty("spring.security.oauth2.client.registration.exgen.client-id");
    String clientSecret = environment.getProperty("spring.security.oauth2.client.registration.exgen.client-secret");

    if (clientId == null || clientSecret == null) {
      throw new IllegalStateException("OAuth2 client credentials not found in configuration");
    }

    ClientRegistration registration = ClientRegistration.withRegistrationId("exgen")
            .clientId(clientId)
            .clientSecret(clientSecret)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .tokenUri(tokenUri)
            .scope("write")
            .build();

    System.out.println("🔐 OAuth2 configured:");
    System.out.println("  Client ID: " + registration.getClientId());
    System.out.println("  Token URI: " + registration.getProviderDetails().getTokenUri());

    return new InMemoryClientRegistrationRepository(registration);
  }


  private String getTokenUriFromEureka(DiscoveryClient discoveryClient) {
    try {
      List<ServiceInstance> instances = discoveryClient.getInstances("auth");
      if (instances != null && !instances.isEmpty()) {
        String url = instances.get(0).getUri() + "/oauth2/token";
        System.out.println("✅ Using discovered auth service: " + url);
        return url;
      }
    } catch (Exception e) {
      System.out.println("⚠️ Eureka discovery failed: " + e.getMessage());
    }

    String fallback = "http://localhost:9000/oauth2/token";
    System.out.println("🔄 Using fallback: " + fallback);
    return fallback;
  }
}