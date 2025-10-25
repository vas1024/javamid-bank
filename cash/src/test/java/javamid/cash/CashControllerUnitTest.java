package javamid.cash;


import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class CashControllerUnitTest {

  @Test
  public void testCashDtoCreation() {
    // Просто проверяем что DTO работает
    CashDto dto = new CashDto();
    dto.setUserId(1L);
    dto.setCurrency("USD");
    dto.setValue(BigDecimal.valueOf(100.0));

    assertThat(dto.getUserId()).isEqualTo(1L);
    assertThat(dto.getCurrency()).isEqualTo("USD");
    assertThat(dto.getValue()).isEqualTo(BigDecimal.valueOf(100.0));
  }
}