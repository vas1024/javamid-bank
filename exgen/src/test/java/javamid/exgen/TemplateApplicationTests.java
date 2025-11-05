package javamid.exgen;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.listener.auto-startup=false", // Отключаем запуск listener'ов
})
class TemplateApplicationTests {

  @MockBean
  private ClientRegistrationRepository clientRegistrationRepository;

  @MockBean
  private OAuth2AuthorizedClientManager authorizedClientManager;

  @MockBean(name = "authRestTemplate")
  private RestTemplate authRestTemplate;

  @MockBean(name = "plainRestTemplate")
  private RestTemplate plainRestTemplate;


	@Test
	void contextLoads() {
	}


}
