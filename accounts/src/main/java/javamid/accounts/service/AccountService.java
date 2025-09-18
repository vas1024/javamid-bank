package javamid.accounts.service;

import jakarta.transaction.Transactional;
import javamid.accounts.model.Account;
import javamid.accounts.model.CashDto;
import javamid.accounts.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AccountService {

  private final AccountRepository accountRepository;

  public AccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }



  public Optional<Account> createAccount(Long userId, String currency, BigDecimal initialBalance) {
    if (accountRepository.existsByUserIdAndCurrency(userId, currency)) {
      return Optional.empty();
    }
    Account account = new Account(userId, currency, initialBalance);
    System.out.println( "hello from accountService! account id " + account.getId() + "  userId " + account.getUserId() + "  balance " + account.getBalance() );
    return Optional.of(accountRepository.save(account));
  }


  public Optional<Account> getAccount(Long userId, String currency) {
    return accountRepository.findByUserIdAndCurrency(userId, currency);
  }

  public List<Account> getUserAccounts(Long userId) {
    return accountRepository.findByUserId(userId);
  }




  public String deposit( CashDto cashDto ){

    if (cashDto == null) { return ("Error: CashDto cannot be null"); }
    Long userId = cashDto.getUserId();
    BigDecimal value = cashDto.getValue();
    String currency = cashDto.getCurrency();
    if (userId == null) { return "Error: User ID cannot be null"; }
    System.out.println("AccountService: deposit: value: " + value );
    if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) { return "Error: Value must be positive"; }
    if (currency == null || currency.trim().isEmpty()) { return "Currency cannot be null or empty"; }

    Optional<Account> accountOptional = accountRepository.findByUserIdAndCurrency(userId,currency);
    if( accountOptional.isEmpty() ) {
      Account newAccount = new Account(userId, currency, value);
      accountRepository.save( newAccount );
    }
    else {
      Account account = accountOptional.get();
      BigDecimal newBalance = account.getBalance().add(value);
      account.setBalance( newBalance );
      accountRepository.save(account);
    }

    Account account = accountRepository.findByUserIdAndCurrency(userId,currency).get();
    System.out.println( "user " + account.getUserId() + " has account of " + account.getBalance() + " " + account.getCurrency() );
    return "success";

  }


  public String withdrawal( CashDto cashDto ){

    System.out.println("withdrawal cashDto " + cashDto.getUserId()+" "+cashDto.getValue()+" "+cashDto.getCurrency());
    String result = "";
    if (cashDto == null) { return ("Error: CashDto cannot be null"); }
    Long userId = cashDto.getUserId();
    BigDecimal value = cashDto.getValue();
    String currency = cashDto.getCurrency();
    if (userId == null) { return "Error: User ID cannot be null"; }
    if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) { return "Error: Value must be positive"; }
    if (currency == null || currency.trim().isEmpty()) { return "Currency cannot be null or empty"; }

    Optional<Account> accountOptional = accountRepository.findByUserIdAndCurrency(userId,currency);
    if( accountOptional.isEmpty() ) {
      return "Error: no such account";
    }
    else if ( accountOptional.get().getBalance().compareTo(BigDecimal.ZERO) == 0 ) {
      return "Error: balance is 0";
    }
    else {
      Account account = accountOptional.get();
      BigDecimal newBalance = account.getBalance().subtract(value);
      if( newBalance.compareTo(BigDecimal.ZERO) >= 0 ) account.setBalance( newBalance );
      else {
        result = "Warning: " + account.getBalance().toString();
        account.setBalance(BigDecimal.ZERO);
      }
      accountRepository.save(account);
    }

    Account account = accountRepository.findByUserIdAndCurrency(userId,currency).get();
    System.out.println( "user " + account.getUserId() + " has account of " + account.getBalance() + " " + account.getCurrency() );


    if( result.isEmpty() ) return  "Success: ";
    else return result;

  }



}