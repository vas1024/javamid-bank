package javamid.transfer.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RestTemplateConfig {

  private final TokenInterceptor tokenInterceptor;

  public RestTemplateConfig(TokenInterceptor tokenInterceptor) {
    this.tokenInterceptor = tokenInterceptor;
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    RestTemplate restTemplate = builder.build();
    restTemplate.setInterceptors(Collections.singletonList(tokenInterceptor));
    return restTemplate;
  }

}