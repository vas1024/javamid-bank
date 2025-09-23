package javamid.transfer.client;

import javamid.transfer.model.ExchangeRateDto;
import javamid.transfer.model.TransferDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


@Component
public class AllClients {
  private final RestTemplate restTemplate;
  public AllClients(RestTemplate restTemplate ){ this.restTemplate = restTemplate; }

  public List<ExchangeRateDto> getExchangeRates() {

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

  public boolean isTransferBlocked( TransferDto transferDto ) {
    String blockResult = "";
    try {
      blockResult = restTemplate.postForObject(
              "http://gateway/blocker/api/transfer",
              transferDto,
              String.class
      );
    } catch (Exception e) {
      System.out.println("TransferService: problem calling blocker: " + e.getMessage());
    }
    if ("BLOCKED".equals(blockResult)) return true;

    return false;   // если сервис blocker недоступен, считаем, что перевод разрешен

  }


  public String transfer( TransferDto transferDto ){
    String result = "";
    try {
      result = restTemplate.postForObject(
              "http://gateway/accounts/api/users/{userId}/accounts/transfer",
              transferDto,
              String.class,
              transferDto.getUserIdFrom()
      );
    } catch (Exception e) {
      result = "Error: " + e.getMessage();
    }

    return result;
  }




}
