package javamid.blocker;

import javamid.blocker.model.TransferDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class BlockerController {

  private final Random random = new Random();


  @PostMapping("/transfer")
  public ResponseEntity<?> postTransfer(@RequestBody TransferDto transferDto) {

    String result;
    if( random.nextInt(10) < 3 ) result = "BLOCKED";
    else result = "PASSED";
    return ResponseEntity.ok().body(result);
  }

}
