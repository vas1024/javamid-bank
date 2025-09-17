package javamid.accounts.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "accounts")
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, length = 3)
  private String currency; // USD, EUR, RUB, etc.
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal balance = BigDecimal.ZERO;
  @Column(nullable = false)
  private Long userId;


  public Account() {}

  public Account(String currency, BigDecimal balance) {
    this.currency = currency;
    this.balance = balance;
  }

  public Account(Long userId, String currency, BigDecimal initialBalance) {
    this.userId = userId;
    this.currency = currency;
    this.balance = initialBalance;
  }


  public Long getId() { return id; }
  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }
  public BigDecimal getBalance() { return balance; }
  public void setBalance(BigDecimal balance) { this.balance = balance; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }

}
