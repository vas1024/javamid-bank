package javamid.accounts.controller;

import javamid.accounts.model.Account;
import javamid.accounts.service.AccountService;
import javamid.accounts.model.CashDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{userId}/accounts")
public class AccountController {

  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }


  @GetMapping()
  public ResponseEntity<List<Account>> getUserAccounts(@PathVariable Long userId) {
    List<Account> accounts = accountService.getUserAccounts(userId);
    return ResponseEntity.ok(accounts);
  }


  @PostMapping
  public ResponseEntity<Account> createAccount(
          @PathVariable Long userId,
          @RequestBody Map<String, Object> request) {
/*
    POST /api/users/123/accounts
    Content-Type: application/json
    {
      "currency": "USD",
            "balance": 100.50
    }
*/
    String currency = (String) request.get("currency");
    BigDecimal balance = new BigDecimal(request.get("balance").toString());

    System.out.println("Hello from AccountContorller!  balance " + balance );


    return accountService.createAccount(userId, currency, balance)
            .map(account -> ResponseEntity.ok()
                    .header("Custom-Header", "value")
                    .body(account))
            .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build()); // 409

  }


  @PostMapping("/deposit")
  public ResponseEntity<?> postDeposit( @RequestBody  CashDto cashDto ){
    System.out.println( "AcountController: deposit: value " + cashDto.getValue() + "  currency "+cashDto.getCurrency()+"  userId "+cashDto.getUserId() );
    String result = accountService.deposit( cashDto );
/*
    if( result.startsWith("Error")){
      return ResponseEntity.badRequest().body(result);
    }
 */
    return ResponseEntity.ok().body(result);
  }

  @PostMapping("/withdrawal")
  public ResponseEntity<?> postWithdrawal( @RequestBody  CashDto cashDto ){
    System.out.println( "AcountController: deposit: value " + cashDto.getValue() + "  currency "+cashDto.getCurrency()+"  userId "+cashDto.getUserId() );
    String result = accountService.withdrawal( cashDto );
/*
    if( result.startsWith("Error")){
      return ResponseEntity.badRequest().body(result);
    }
 */
    return ResponseEntity.ok().body(result);
  }


}