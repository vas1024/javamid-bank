package javamid.front.controller;

import jakarta.servlet.http.HttpServletRequest;
import javamid.front.model.ExchangeRateDto;
import javamid.front.model.Notification;
import javamid.front.util.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class ProxyController {
  private final RestTemplate restTemplate;
  private final AuthUtils authUtils;
  public ProxyController( RestTemplate restTemplate,
                          AuthUtils authUtils ) {
    this.restTemplate = restTemplate;
    this.authUtils = authUtils;
  }

  @GetMapping( "/api/notifications/{userId}" )
  public ResponseEntity<?> getNotify(@PathVariable Long userId,
                                     HttpServletRequest request ) {

    try {
      String targetUrl = "http://gateway/notify/api/notifications/" + userId;
      String queryString = request.getQueryString();
      if (queryString != null) {
        targetUrl += "?" + queryString;
      }
      List<Notification> notifications = restTemplate.getForObject(
              targetUrl,
              List.class
      );
      return ResponseEntity.ok(notifications);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error contacting notification service: " + e.getMessage());
    }
  }



    @GetMapping( "/api/rates" )
    public List<ExchangeRateDto> getExchangeRates(HttpServletRequest request ) {

      try {
        String targetUrl = "http://gateway/exchange/api/rates";

        List<ExchangeRateDto> rates = restTemplate.getForObject(
                targetUrl,
                List.class
        );
        return rates;
      } catch (Exception e) {
        System.out.println("can't get exchange rates " + e.getMessage());
        return new ArrayList<>();
      }



    }

}
