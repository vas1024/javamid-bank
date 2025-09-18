package javamid.accounts.model;

import java.math.BigDecimal;

public class CashDto {
  private Long userId;
  private BigDecimal value;
  private String currency;

  public CashDto(){};
  public CashDto(Long userId, BigDecimal value, String currency){
    this.userId = userId;
    this.setValue( value );
    this.setCurrency( currency );
  }
  public void setUserId( Long userId ){ this.userId = userId; }
  public void setValue( BigDecimal value ){
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Value cannot be negative");
    }
    this.value = value;
  }
  public void setCurrency( String currency ){
    if (currency == null) {
      throw new IllegalArgumentException("Currency cannot be null");
    }
    this.currency = currency.toUpperCase();
  }
  public Long getUserId() { return userId; }
  public BigDecimal getValue(){ return value ; }
  public String getCurrency(){return currency; }

}
