package javamid.cash;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class CashController {

  @PostMapping("/withdrawal")
  public ResponseEntity<?> postWithdrawal( SumDto sum ){
    return ResponseEntity.ok().build();
  }

  @PostMapping("/deposit")
  public ResponseEntity<?> postDeposit( SumDto sum){



    return ResponseEntity.ok().build();
  }
}
