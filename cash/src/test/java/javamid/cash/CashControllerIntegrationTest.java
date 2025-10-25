package javamid.cash;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CashControllerIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  private String getBaseUrl() {
    return "http://localhost:" + port + "/api";
  }

  @Test
  public void testWithdrawal_ReturnsOk() {
    // Given
    CashDto cashDto = new CashDto();
    cashDto.setUserId(123L);
    cashDto.setCurrency("USD");
    cashDto.setValue(BigDecimal.valueOf(100.0));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<CashDto> request = new HttpEntity<>(cashDto, headers);

    // When
    ResponseEntity<String> response = restTemplate.postForEntity(
            getBaseUrl() + "/withdrawal",
            request,
            String.class
    );

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    System.out.println("Withdrawal response: " + response.getBody());
  }

  @Test
  public void testDeposit_ReturnsOk() {
    // Given
    CashDto cashDto = new CashDto();
    cashDto.setUserId(456L);
    cashDto.setCurrency("EUR");
    cashDto.setValue(BigDecimal.valueOf(50.0));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<CashDto> request = new HttpEntity<>(cashDto, headers);

    // When
    ResponseEntity<String> response = restTemplate.postForEntity(
            getBaseUrl() + "/deposit",
            request,
            String.class
    );

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    System.out.println("Deposit response: " + response.getBody());
  }
}