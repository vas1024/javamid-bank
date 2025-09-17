package javamid.accounts.repository;

import javamid.accounts.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

  List<Account> findByUserId(Long userId);

  Optional<Account> findByUserIdAndCurrency(Long userId, String currency);

  boolean existsByUserIdAndCurrency(Long userId, String currency);

}