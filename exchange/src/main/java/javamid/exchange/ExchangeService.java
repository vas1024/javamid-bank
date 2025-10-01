package javamid.exchange;


import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.LinkedHashMap;
import java.util.Map;

  @Service
  public class ExchangeService {


    private final Map<String, Double> exchangeRates = new LinkedHashMap<>();


    @PostConstruct
    public void init() {
      exchangeRates.put("RUB", 1.0);
      exchangeRates.put("USD", 80.0);
      exchangeRates.put("EUR", 100.5);
      exchangeRates.put("CNY", 11.75);
      exchangeRates.put("GBP", 150.75);
    }

    public Map<String, Double> getAllRates() {
      return exchangeRates;
    }

    public Double getRate(String currency) {
      Double rate = exchangeRates.get(currency.toUpperCase());
      if (rate == null) {
        return 1d;
      }
      return rate;
    }

    public void updateRate(String currency, Double newRate) {
      exchangeRates.put(currency.toUpperCase(), newRate);
    }
  }