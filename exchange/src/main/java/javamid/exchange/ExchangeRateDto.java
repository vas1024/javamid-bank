package javamid.exchange;

public class ExchangeRateDto {
  private String title;
  private String name;
  private double value;

  public ExchangeRateDto(){}
  
  public ExchangeRateDto(String title, String name, double value) {
    this.title = title;
    this.name = name;
    this.value = value;
  }

  public String getTitle() {  return title;  }
  public String getName() { return name;  }
  public double getValue() { return value;  }

}