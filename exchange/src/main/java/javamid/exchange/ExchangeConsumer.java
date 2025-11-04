package javamid.exchange;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class ExchangeConsumer {

  private final ExchangeService exchangeService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ExchangeConsumer( ExchangeService exchangeService ) { this.exchangeService = exchangeService ; }


  @KafkaListener(topics = "rates", groupId = "exchange")
  public void consumeRates(String jsonArray) {
    try {
      List<ExchangeRateDto> rates = objectMapper.readValue(
              jsonArray,
              new TypeReference<List<ExchangeRateDto>>() {}
      );

      System.out.println("✅ Получены курсы: " + rates.size() + " записей");

      for (ExchangeRateDto rate : rates) {
        System.out.println(rate.getTitle() + " (" + rate.getName() + "): " + rate.getValue());

        if (rate.getName() != null && rate.getValue() > 0) {
          exchangeService.updateRate(rate.getName().toUpperCase(), rate.getValue());
        }
      }

    } catch (Exception e) {
      System.err.println("❌ Ошибка десериализации: " + e.getMessage());
    }
  }


}

