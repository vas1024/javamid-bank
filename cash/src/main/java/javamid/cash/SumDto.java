package javamid.cash;

import java.math.BigDecimal;

public class SumDto {
  private BigDecimal value;
  private String currency;

  SumDto(){};
  SumDto(BigDecimal value, String currency){
    SumDto sum = new SumDto();
    sum.setValue( value );
    sum.setCurrency( currency );
  }
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
  public BigDecimal getValue(){ return value ; }
  public String getCurrency(){return currency; }

}
