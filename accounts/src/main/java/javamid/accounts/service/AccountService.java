package javamid.accounts.service;

import jakarta.transaction.Transactional;
import javamid.accounts.model.Account;
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


  public Optional<Account> withdraw(Long userId, String currency, BigDecimal amount) {
    return accountRepository.findByUserIdAndCurrency(userId, currency)
            .filter(account -> account.getBalance().compareTo(amount) >= 0)
            .map(account -> {
              account.setBalance(account.getBalance().subtract(amount));
              return accountRepository.save(account);
            });
  }
}