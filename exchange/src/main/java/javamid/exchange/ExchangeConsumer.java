package javamid.exchange;


import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
  private final MeterRegistry meterRegistry;
  private Timer.Sample delayMetricTimer;
  private final Gauge delayGauge;
  private long lastUpdateTime;
  public ExchangeConsumer( ExchangeService exchangeService,
                           MeterRegistry meterRegistry ) {
    this.exchangeService = exchangeService ;
    this.meterRegistry = meterRegistry;
    this.delayMetricTimer = Timer.start(meterRegistry);
    lastUpdateTime = System.currentTimeMillis();
    delayGauge = Gauge.builder( "bank_exchange_rate_seconds_since_last_update", this, consumer ->
            ( System.currentTimeMillis() -  consumer.lastUpdateTime )  / 1000 )
            .description("Number of seconds since last update of exchange rates in bank application")
            .register(meterRegistry);
  }


  @KafkaListener(topics = "rates", groupId = "exchange")
  public void consumeRates(String jsonArray) {
    try {
      List<ExchangeRateDto> rates = objectMapper.readValue(
              jsonArray,
              new TypeReference<List<ExchangeRateDto>>() {}
      );


      delayMetricTimer.stop( meterRegistry.timer("bank_exchange_rates_update") );
      System.out.println("✅ Получены курсы: " + rates.size() + " записей");
      delayMetricTimer = Timer.start(meterRegistry);
      lastUpdateTime = System.currentTimeMillis();


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

