package javamid.exchange;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Component
public class ExchangeConsumer {

  private final ExchangeService exchangeService;

  public ExchangeConsumer( ExchangeService exchangeService ) { this.exchangeService = exchangeService ; }

  @KafkaListener(topics = "rates", groupId = "exchange")
  public void consumeRates(@Payload List<ExchangeRateDto> rates) {
    System.out.println("✅ Получены курсы: " + rates.size() + " записей");

    for (Object item : rates) {
      System.out.println("item is of Class:" + item.getClass());
      System.out.println( item );

    }

  }
}

