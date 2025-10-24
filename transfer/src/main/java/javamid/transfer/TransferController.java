package javamid.transfer;

import jakarta.servlet.http.HttpServletRequest;
import javamid.transfer.model.TransferDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class TransferController {

  private final TransferService transferService;
    public TransferController(TransferService transferService
                             ){
    this.transferService = transferService ;
  }


  @PostMapping("/transfer")
  public ResponseEntity<?> postTransfer(@RequestBody TransferDto transferDto,
                                        HttpServletRequest request  ) {

    System.out.println("TransferController: postTransfer: transferDto: valueFrom To " + transferDto.getValueFrom() + " " + transferDto.getValueTo());

    String result = transferService.transfer(transferDto);

    return ResponseEntity.ok().body(result);

  }

}
