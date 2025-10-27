package javamid.auth.client;

import javamid.auth.model.AuthRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AccountsClient {

  private final RestTemplate restTemplate;
  public AccountsClient(RestTemplate restTemplate ){ this.restTemplate = restTemplate; }

  public Long checkLogin( AuthRequest authRequest ){
    Long result = 01L;
    try {
      result = restTemplate.postForObject(
//              "http://gateway/accounts/api/users/login",
              "http://accounts:8080/api/users/login",
              authRequest,
              Long.class
      );
    } catch (Exception e) {
      System.out.println( e.getMessage() );
      return -1L;
    }
    return result;
  }

}
