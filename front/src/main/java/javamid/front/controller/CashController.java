package javamid.front.controller;

import jakarta.servlet.http.HttpServletRequest;
import javamid.front.model.CashDto;
import javamid.front.util.AuthUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Controller
public class CashController {

  private final RestTemplate restTemplate;
  private final AuthUtils authUtils;

  public CashController(RestTemplate restTemplate,
                        AuthUtils authUtils) {
    this.restTemplate = restTemplate;
    this.authUtils = authUtils;
  }

  @PostMapping("/user/{id}/cash")
  public String postCash(@PathVariable Long id,
                         @RequestParam BigDecimal value,
                         @RequestParam String currency,
                         @RequestParam String action,
                         RedirectAttributes redirectAttributes,
                         HttpServletRequest request) {

    System.out.println("CashController: postCash: value " + value + "  currency " + currency + "  action " + action );
    CashDto cashDto = new CashDto(id, value, currency);
    System.out.println("CashController: postCash: cashDto: value " + cashDto.getValue() + "  currency " + cashDto.getCurrency() + "  action "  );

    HttpEntity<CashDto> entity = authUtils.createAuthEntityWithBody(cashDto, request);
    if ("PUT".equals(action)) {
      String result;
      try {
/*        result = restTemplate.postForObject(
                "http://gateway/cash/api/deposit",
                cashDto,
                String.class,
                cashDto.getUserId()
        );*/
        ResponseEntity<String> response = restTemplate.exchange(
                "http://gateway/cash/api/deposit",
                HttpMethod.POST,
                entity,
                String.class,
                Map.of("userId", id)
        );
        result = response.getBody();

        if (result != null && result.startsWith("Error")) {
          redirectAttributes.addFlashAttribute("cashErrors", result);
          return "redirect:/" + id;
        }

      } catch (Exception e) {
        redirectAttributes.addFlashAttribute("cashErrors", e);
        return "redirect:/" + id;
      }

      String message = "Деньги " + value + " " + currency + " успешно зачислены. result " + result ;
      redirectAttributes.addFlashAttribute("message", message );
      return "redirect:/" + id;

    } else if ("GET".equals(action)) {

      System.out.println( "CashController: postCash: action " + action );
      String result;
      try {
/*        result = restTemplate.postForObject(
                "http://gateway/cash/api/withdrawal",
                cashDto,
                String.class,
                cashDto.getUserId()
        );*/
        ResponseEntity<String> response = restTemplate.exchange(
                "http://gateway/cash/api/withdrawal",
                HttpMethod.POST,
                entity,
                String.class,
                Map.of("userId", id)
        );
        result = response.getBody();

        if (result != null && result.startsWith("Error")) {
          redirectAttributes.addFlashAttribute("cashErrors", result);
          return "redirect:/" + id;
        }

      } catch (Exception e) {
        redirectAttributes.addFlashAttribute("cashErrors", e);
        return "redirect:/" + id;
      }

      if (result != null && result.startsWith("Warning:")) {
        redirectAttributes.addFlashAttribute("cashErrors",
                "на этом счету больше нет средств. вам выдано " + result.substring(7) + " " + currency );
        return "redirect:/" + id;
      }

      String message = "Вам выдано " + value + " " + currency ;
      redirectAttributes.addFlashAttribute("message", message );
      return "redirect:/" + id;

    }

    redirectAttributes.addFlashAttribute("cashErrors", "unknown action" );
    return "redirect:/" + id;
  }




}
