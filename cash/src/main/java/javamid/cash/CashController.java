package javamid.cash;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class CashController {

  private final RestTemplate restTemplate;

  public CashController( RestTemplate restTemplate ){ this.restTemplate = restTemplate; }



  @PostMapping("/withdrawal")
  public ResponseEntity<?> postWithdrawal(@RequestBody CashDto cashDto) {

    // call blocker
    System.out.println("CashController: postWithdrawal: cashDto: id value currency " + cashDto.getUserId() + cashDto.getCurrency() + cashDto.getValue() );
    String result = "";
    try {
      result = restTemplate.postForObject(
              "http://gateway/accounts/api/users/{userId}/accounts/withdrawal",
              cashDto,
              String.class,
              cashDto.getUserId()
      );

    } catch (Exception e) {
      result = "Error: " + e.getMessage();
    }

    System.out.println( "CashController: withdrawal: result " + result );

    return ResponseEntity.ok().body(result);
  }





  @PostMapping("/deposit")
  public ResponseEntity<?> postDeposit( @RequestBody CashDto cashDto){

    //call blocker

    System.out.println("CashController: postDeposit: cashDto: id value currency " + cashDto.getUserId() + cashDto.getCurrency() + cashDto.getValue() );
    String result = "";
    try {
       result = restTemplate.postForObject(
//            "http://localhost:8082/api/users/{userId}/accounts/deposit",
            "http://gateway/accounts/api/users/{userId}/accounts/deposit",
            cashDto,
            String.class,
            cashDto.getUserId()
       );
    } catch (Exception e) {
      result = result + "Error: " + e.getMessage();
    }

    //call notify

    return ResponseEntity.ok().body(result);


  }
}
