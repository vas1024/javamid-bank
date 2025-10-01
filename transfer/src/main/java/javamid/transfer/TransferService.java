package javamid.transfer;

import javamid.transfer.client.AllClients;
import javamid.transfer.model.ExchangeRateDto;
import javamid.transfer.model.TransferDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransferService {


  private final AllClients allClients;
  public TransferService(AllClients allClients){ this.allClients = allClients ; }

  public String transfer( TransferDto transferDto, String token ) {


    if (allClients.isTransferBlocked(transferDto)) return "Error: blocked";

    Long userIdFrom = transferDto.getUserIdFrom();
    Long userIdTo = transferDto.getUserIdTo();
    BigDecimal valueFrom = transferDto.getValueFrom();
    BigDecimal valueTo = transferDto.getValueTo();
    String currencyFrom = transferDto.getCurrencyFrom();
    String currencyTo = transferDto.getCurrencyTo();

    System.out.println( "TransferService: transferDto  from to " + userIdFrom + " " + valueFrom + " " + currencyFrom
            + " " + userIdTo + " " + valueTo + " " + currencyTo );

    if ( ! currencyFrom.equals(currencyTo)) {
      List<ExchangeRateDto> exchangeRateDtoList = allClients.getExchangeRates();
      double exchangeRateFrom = exchangeRateDtoList.stream()
              .filter(exchangeRateDto -> exchangeRateDto.getName().equals(currencyFrom))
              .findFirst()
              .map(ExchangeRateDto::getValue)
              .orElse(1.0);
      System.out.println("TransferService: exchangeReteFrom " + exchangeRateFrom );

      double exchangeRateTo = exchangeRateDtoList.stream()
              .filter(exchangeRateDto -> exchangeRateDto.getName().equals(currencyTo))
              .findFirst()
              .map(ExchangeRateDto::getValue)
              .orElse(1.0);
      System.out.println("TransferService: exchangeReteTo " + exchangeRateTo );

      valueTo = valueFrom.multiply(BigDecimal.valueOf(exchangeRateFrom))
              .divide(BigDecimal.valueOf(exchangeRateTo),  4, RoundingMode.HALF_UP);


    } else valueTo = valueFrom;

    System.out.println( "TransferService: valueFrom " + valueFrom + "  valueTo " + valueTo );

    transferDto.setValueTo(valueTo);

    System.out.println( "TransferService: before accounts service:  " + transferDto );
    String result = allClients.transfer(transferDto, token);

    if( ! result.startsWith("Error:")) {
      allClients.notify(transferDto);
    }


    return result;

  }

}
