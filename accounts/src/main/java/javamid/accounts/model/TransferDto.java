package javamid.accounts.model;

import java.math.BigDecimal;

public class TransferDto {
  private Long userIdFrom;
  private String currencyFrom;
  private BigDecimal valueFrom;
  private Long userIdTo;
  private String currencyTo;
  private BigDecimal valueTo;

  public TransferDto(){};
  public TransferDto(Long userIdFrom,
                     BigDecimal valueFrom,
                     String currencyFrom,
                     Long userIdTo,
                     BigDecimal valueTo,
                     String currencyTo
                     ){
    this.userIdFrom = userIdFrom;
    this.setValueFrom( valueFrom );
    this.setCurrencyFrom( currencyFrom );
    this.userIdTo = userIdTo;
    this.setValueTo( valueTo );
    this.setCurrencyTo( currencyTo );
  }

  public void setUserIdFrom( Long userIdFrom ){ this.userIdFrom = userIdFrom; }
  public void setValueFrom( BigDecimal value ){
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Value cannot be negative");
    }
    this.valueFrom = value;
  }
  public void setCurrencyFrom( String currency ){
    if (currency == null) {
      throw new IllegalArgumentException("Currency cannot be null");
    }
    this.currencyFrom = currency.toUpperCase();
  }

  public void setUserIdTo( Long userIdTo ){ this.userIdTo = userIdTo; }
  public void setValueTo( BigDecimal valueTo ){
    if (valueTo.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Value cannot be negative");
    }
    this.valueTo = valueTo;
  }
  public void setCurrencyTo( String currencyTo ){
    if (currencyTo == null) {
      throw new IllegalArgumentException("Currency cannot be null");
    }
    this.currencyTo = currencyTo.toUpperCase();
  }

  public Long getUserIdFrom() { return userIdFrom; }
  public BigDecimal getValueFrom(){ return valueFrom ; }
  public String getCurrencyFrom(){return currencyFrom; }
  public Long getUserIdTo() { return userIdTo; }
  public BigDecimal getValueTo(){ return valueTo ; }
  public String getCurrencyTo(){return currencyTo; }
}
