
package javamid.exgen;

import javamid.exgen.controller.ExgenController;
import javamid.exgen.model.ExchangeRateDto;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExgenControllerMockProducerTest {

  private MockProducer<String, List<ExchangeRateDto>> mockProducer;
  private ExgenController exgenController;
  private RestTemplate plainRestTemplate;
  private RestTemplate authRestTemplate;
  private OAuth2AuthorizedClientManager clientManager;


  @Test
  void simpleExgenControllerTest() {
    // 1. MockProducer
    MockProducer<String, List<ExchangeRateDto>> mockProducer =
            new MockProducer<>(true, new StringSerializer(), new JsonSerializer<>());

    // 2. KafkaTemplate с MockProducer
    KafkaTemplate<String, List<ExchangeRateDto>> kafkaTemplate =
            new KafkaTemplate<>(new DefaultKafkaProducerFactory<String, List<ExchangeRateDto>>(Collections.emptyMap()) {
              @Override
              public org.apache.kafka.clients.producer.Producer<String, List<ExchangeRateDto>> createProducer() {
                return mockProducer;
              }
            });

    // 3. Мокаем OAuth2
    OAuth2AuthorizedClientManager clientManager = mock(OAuth2AuthorizedClientManager.class);
    OAuth2AuthorizedClient client = mock(OAuth2AuthorizedClient.class);
    OAuth2AccessToken token = mock(OAuth2AccessToken.class);
    when(token.getTokenValue()).thenReturn("test-token");
    when(client.getAccessToken()).thenReturn(token);
    when(clientManager.authorize(any())).thenReturn(client);

    // 4. Создаем контроллер
    ExgenController controller = new ExgenController(null, null, clientManager, kafkaTemplate);

    // 5. Тестовые данные
    List<ExchangeRateDto> rates = Arrays.asList(
            new ExchangeRateDto("Доллар США", "USD", 85.0)
    );

    // 6. Вызываем
    controller.sendRates(rates);

    // 7. Проверяем
    assertThat(mockProducer.history()).hasSize(1);
    assertThat(mockProducer.history().get(0).topic()).isEqualTo("rates");
    assertThat(mockProducer.history().get(0).value()).isEqualTo(rates);
  }

}