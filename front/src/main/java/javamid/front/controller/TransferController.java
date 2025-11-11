package javamid.front.controller;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import javamid.front.model.CashDto;
import javamid.front.model.TransferDto;
import javamid.front.util.AuthUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

@Controller
public class TransferController {

  private final RestTemplate restTemplate;
  private final AuthUtils authUtils;
  private final MeterRegistry meterRegistry;
  public TransferController(RestTemplate restTemplate,
                            AuthUtils authUtils,
                            MeterRegistry meterRegistry ) {
    this.restTemplate = restTemplate;
    this.authUtils = authUtils;
    this.meterRegistry = meterRegistry;
  }

  @PostMapping("/user/{id}/transfer")
  public String postCash(@PathVariable Long id,
                         @RequestParam BigDecimal value,
                         @RequestParam String from_currency,
                         @RequestParam String to_currency,
                         @RequestParam Long to_id,
                         RedirectAttributes redirectAttributes,
                         HttpServletRequest request ) {

    System.out.println("TransferController: transfer: value " + value +
            "  from_currency " + from_currency + "  to_currency " + to_currency + "   from id " + id + "  to_id " + to_id );

//    Long userIdTo = Long.parseLong(to_id);

    if( id == to_id && from_currency.equals(to_currency) ) {
      redirectAttributes.addFlashAttribute("transferErrors", "Перевод на тот же самый счет");
      return "redirect:/" + id;
    }
    if( value.compareTo(BigDecimal.ZERO) <= 0 ) {
      redirectAttributes.addFlashAttribute("transferErrors", "Сумма перевода должна быть положительным числом");
      return "redirect:/" + id;
    }

    TransferDto transferDto = new TransferDto(
            id,
            value,
            from_currency,
            to_id,
            value,
            to_currency
    );

    HttpEntity<TransferDto> entity = authUtils.createAuthEntityWithBody(transferDto, request);

    String result;
    try {
/*      result = restTemplate.postForObject(
              "http://gateway/transfer/api/transfer",
              transferDto,
              String.class
      );*/

      ResponseEntity<String> response = restTemplate.exchange(
//              "http://gateway/transfer/api/transfer",
              "http://transfer:8080/api/transfer",
              HttpMethod.POST,
              entity,
              String.class
      );
      result = response.getBody();

      if (result != null && result.startsWith("Error")) {
        meterRegistry.counter("bank_transfer_fail",
                "from_user", transferDto.getUserIdFrom().toString(),
                "to_user", transferDto.getUserIdTo().toString(),
                "from_account", transferDto.getCurrencyFrom(),
                "to_account", transferDto.getCurrencyTo()
        ).increment();
        redirectAttributes.addFlashAttribute("transferErrors", result);
        return "redirect:/" + id;
      }


    } catch (Exception e) {
      meterRegistry.counter("bank_transfer_fail",
              "from_user", transferDto.getUserIdFrom().toString(),
              "to_user", transferDto.getUserIdTo().toString(),
              "from_account", transferDto.getCurrencyFrom(),
              "to_account", transferDto.getCurrencyTo()
      ).increment();
      redirectAttributes.addFlashAttribute("transferErrors", e);
      return "redirect:/" + id;
    }


    meterRegistry.counter("bank_transfer_success",
            "from_user", transferDto.getUserIdFrom().toString(),
            "to_user", transferDto.getUserIdTo().toString(),
            "from_account", transferDto.getCurrencyFrom(),
            "to_account", transferDto.getCurrencyTo()
    ).increment();

    String message = "зачислено " + result + " " + to_currency ;
    redirectAttributes.addFlashAttribute("message", message );
    return "redirect:/" + id;
  }




}
