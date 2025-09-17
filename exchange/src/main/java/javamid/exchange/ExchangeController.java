package javamid.exchange;


import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

  @RestController
  @CrossOrigin(origins = "*")
  @RequestMapping("/api")
  public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
      this.exchangeService = exchangeService;
    }

    @GetMapping("/rates")
    public List<ExchangeRateDto> getAllRates() {

      Map<String, Double> allRates = exchangeService.getAllRates();
      System.out.println("allRates " + allRates);
      List<ExchangeRateDto> result = new ArrayList<>();


      for (Map.Entry<String, Double> entry : allRates.entrySet()) {
        String currencyCode = entry.getKey();
        Double rateValue = entry.getValue();
        ExchangeRateDto dto = new ExchangeRateDto(
                getCurrencyTitle(currencyCode),
                currencyCode,
                rateValue
        );
        result.add(dto);
      }
      return result;
    }
    private String getCurrencyTitle(String currencyCode) {
      return switch (currencyCode) {
        case "RUB" -> "Российский рубль";
        case "USD" -> "Доллар США";
        case "EUR" -> "Евро";
        case "GBP" -> "Фунт стерлингов";
        case "CNY" -> "Китайский юань";
        case "JPY" -> "Японская иена";
        default -> currencyCode;
      };
    }


    /*
    @GetMapping("/rates")
    public Map<String, Double> getAllRates() {
      return exchangeService.getAllRates();
    }
    */

    @GetMapping("/{currency}")
    public Double getRate(@PathVariable String currency) {
      return exchangeService.getRate(currency);
    }

    @PostMapping("/{currency}")
    public void updateRate(@PathVariable String currency, @RequestParam Double newRate) {
      exchangeService.updateRate(currency, newRate);
    }

    @PostMapping("/bulk")
    public void updateRatesBulk(@RequestBody List<ExchangeRateDto> currencyRates) {
      currencyRates.forEach(rateDto -> {
        if (rateDto.getName() != null && rateDto.getValue() > 0) {
          exchangeService.updateRate(rateDto.getName().toUpperCase(), rateDto.getValue());
        }
      });
    }

  }
