package javamid.exgen.controller;

import javamid.exgen.model.ExchangeRateDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class ExgenController {

  private final RestTemplate plainRestTemplate;
  private final RestTemplate authRestTemplate;
  private final Random random = new Random();
  private final OAuth2AuthorizedClientManager clientManager;
  private final KafkaTemplate<String, List<ExchangeRateDto>> kafkaTemplate;

  public ExgenController(
                          @Qualifier("plainRestTemplate") RestTemplate plainRestTemplate,
                          @Qualifier("authRestTemplate") RestTemplate authRestTemplate,
                          OAuth2AuthorizedClientManager clientManager,
                          KafkaTemplate<String, List<ExchangeRateDto>> kafkaTemplate ){
    this.plainRestTemplate = plainRestTemplate;
    this.authRestTemplate = authRestTemplate;
    this.clientManager = clientManager;
    this.kafkaTemplate = kafkaTemplate;
  }

  public List<ExchangeRateDto> getRates() {

    try {
      // Используем ParameterizedTypeReference для работы с List<>
      ResponseEntity<List<ExchangeRateDto>> response = plainRestTemplate.exchange(
//              "http://gateway/exchange/api/rates",
              "http://exchange:8080/api/rates",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<ExchangeRateDto>>() {
              }
      );

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        return response.getBody();
      } else {
        System.out.println("Не удалось получить курсы: " + response.getStatusCode());
        return new ArrayList<>();
      }
    } catch ( Exception e ) {
      System.out.println("Полная ошибка при получении курсов: " + e );
    }
    return new ArrayList<>();
  }


  public List<ExchangeRateDto> modifyRates(List<ExchangeRateDto> originalRates) {
    List<ExchangeRateDto> modifiedRates = new ArrayList<>();

    for (ExchangeRateDto originalRate : originalRates) {
      ExchangeRateDto modifiedRate = new ExchangeRateDto();

      modifiedRate.setTitle(originalRate.getTitle());
      modifiedRate.setName(originalRate.getName());

      double changePercent = (random.nextDouble() * 0.02) - 0.01; // -0.01 до +0.01
      double newValue = originalRate.getValue() * (1 + changePercent);
      // Округляем до 2 знаков после запятой
      newValue = Math.round(newValue * 100.0) / 100.0;
      if( ! "RUB".equalsIgnoreCase(originalRate.getName()) ) {
        modifiedRate.setValue(newValue);
      }
      modifiedRates.add(modifiedRate);
    }

    return modifiedRates;
  }


  // Отправляем обновленные курсы
  public void sendRates(List<ExchangeRateDto> rates) {

    // 🔽 1. ПОЛУЧАЕМ ACCESS TOKEN
    System.out.println( "получаем access token");
    OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId("exgen")
            .principal("exgen")
            .build();
    OAuth2AuthorizedClient client = clientManager.authorize(request);
    String accessToken = client.getAccessToken().getTokenValue();

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<List<ExchangeRateDto>> entity = new HttpEntity<>(rates, headers);

    try {
      ResponseEntity<Void> response = plainRestTemplate.exchange(
//              "http://gateway/exchange/api/bulk",
              "http://exchange:8080/api/bulk",
              HttpMethod.POST,
              entity,
              Void.class
      );
    } catch ( Exception e ) {
      System.out.println("❌ Общая ошибка: " + e.getMessage());
      e.printStackTrace();
    }




    try {
      kafkaTemplate.send("rates", rates);

      System.out.println("✅ " + rates.size() + " курсов отправлены в Kafka (at most once)");

    } catch (Exception e) {
      // At most once - не пытаемся повторить при ошибках
      System.out.println("❌ Ошибка отправки: " + e.getMessage());
    }



  }


}
