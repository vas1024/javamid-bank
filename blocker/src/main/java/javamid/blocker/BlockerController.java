package javamid.blocker;

import io.micrometer.core.instrument.MeterRegistry;
import javamid.blocker.model.TransferDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class BlockerController {

  private final Random random = new Random();
  private final MeterRegistry meterRegistry;
  public BlockerController( MeterRegistry meterRegistry ) { this.meterRegistry = meterRegistry; }

  @PostMapping("/transfer")
  public ResponseEntity<?> postTransfer(@RequestBody TransferDto transferDto) {

    String result;
    if( random.nextInt(10) < 3 ) result = "BLOCKED";
    else result = "PASSED";

    if( "BLOCKED".equals( result ) ){
      meterRegistry.counter("bank_transfer_blocked",
              "from_user", transferDto.getUserIdFrom().toString(),
              "to_user", transferDto.getUserIdTo().toString(),
              "from_account", transferDto.getCurrencyFrom(),
              "to_account", transferDto.getCurrencyTo()
      ).increment();
    }

    return ResponseEntity.ok().body(result);
  }

}
