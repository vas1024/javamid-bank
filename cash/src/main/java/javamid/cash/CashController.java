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
  public ResponseEntity<?> postWithdrawal( CashDto sum ){
    return ResponseEntity.ok().build();
  }

  @PostMapping("/deposit")
  public ResponseEntity<?> postDeposit( @RequestBody CashDto cashDto){

    //call blocker

    System.out.println("CashController: postDeposit: cashDto: id value currency " + cashDto.getUserId() + cashDto.getCurrency() + cashDto.getValue() );
    String result = restTemplate.postForObject(
            "http://localhost:8082/api/users/{userId}/accounts/deposit",
            cashDto,
            String.class,
            cashDto.getUserId()
    );

    if (result.startsWith("Error")) {
      return ResponseEntity.badRequest().body(result);
    }

    //call notify

    return ResponseEntity.ok().body(result);


  }
}
