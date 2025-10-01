package javamid.transfer;

import jakarta.servlet.http.HttpServletRequest;
import javamid.transfer.model.TransferDto;
import javamid.transfer.util.AuthUtils;
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
  private final AuthUtils authUtils;
  public TransferController(TransferService transferService,
                            AuthUtils authUtils ){
    this.transferService = transferService ;
    this.authUtils = authUtils;
  }


  @PostMapping("/transfer")
  public ResponseEntity<?> postTransfer(@RequestBody TransferDto transferDto,
                                        HttpServletRequest request  ) {

    System.out.println("TransferController: postTransfer: transferDto: valueFrom To " + transferDto.getValueFrom() + " " + transferDto.getValueTo());


    String token = authUtils.extractTokenFromRequest(request);

    String result = transferService.transfer(transferDto, token);

    return ResponseEntity.ok().body(result);

  }

}
