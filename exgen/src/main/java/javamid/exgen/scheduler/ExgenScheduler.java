package javamid.exgen.scheduler;

import javamid.exgen.controller.ExgenController;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExgenScheduler {


  private final ExgenController exgenController;

  public ExgenScheduler(ExgenController exgenController ) {
    this.exgenController = exgenController;
  }


  @Scheduled(fixedRate = 10000)
  public void updateCurrencyRates() {
    try{
      var rates = exgenController.getRates();
      System.out.println( "получили курсы "  + rates );
      var modRates =  exgenController.modifyRates(rates);
      exgenController.sendRates(modRates);


    } catch (Exception e) {
      System.out.println("Failed to update currency rates" + e.getMessage() );
    }
  }
}