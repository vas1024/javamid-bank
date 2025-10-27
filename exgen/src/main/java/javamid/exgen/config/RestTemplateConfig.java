package javamid.exgen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;



@Configuration
public class RestTemplateConfig {

  // 🔐 RestTemplate с OAuth2 для защищенных endpoints
  @Bean("authRestTemplate")
  public RestTemplate oauth2RestTemplate(OAuth2AuthorizedClientManager authorizedClientManager) {
    RestTemplate restTemplate = new RestTemplate();

    restTemplate.getInterceptors().add((request, body, execution) -> {
      String accessToken = getAccessToken(authorizedClientManager);
      request.getHeaders().setBearerAuth(accessToken);
      return execution.execute(request, body);
    });

    return restTemplate;
  }

  // 🔓 Обычный RestTemplate для незащищенных endpoints
  @Bean("plainRestTemplate")
    public RestTemplate plainRestTemplate() {
    return new RestTemplate();
  }



  private String getAccessToken(OAuth2AuthorizedClientManager authorizedClientManager) {
    try {
      var request = org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
              .withClientRegistrationId("exgen")
              .principal("exgen")
              .build();

      var authorizedClient = authorizedClientManager.authorize(request);

      if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
        return authorizedClient.getAccessToken().getTokenValue();
      } else {
        throw new RuntimeException("Failed to obtain access token");
      }
    } catch (Exception e) {
      throw new RuntimeException("Error getting OAuth2 token: " + e.getMessage(), e);
    }
  }
}


