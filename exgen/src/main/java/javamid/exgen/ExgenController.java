package javamid.exgen;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
public class ExgenController {

  private final RestTemplate restTemplate;
  private final Random random = new Random();

  public ExgenController( RestTemplate restTemplate ){ this.restTemplate = restTemplate; }

  public List<ExchangeRateDto> getRates() {

    // Используем ParameterizedTypeReference для работы с List<>
    ResponseEntity<List<ExchangeRateDto>> response = restTemplate.exchange(
            "http://gateway/exchange/api/rates",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ExchangeRateDto>>() {}
    );

    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
      return response.getBody();
    } else {
      System.out.println("Не удалось получить курсы: " + response.getStatusCode());
      return new ArrayList<>();
    }
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
    String url = "http://gateway/exchange/api/bulk";
    restTemplate.postForObject(url, rates, Void.class);
  }


}
