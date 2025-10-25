package javamid.cash;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest

public class SimpleContextTest {

  @Test
  public void contextLoads() {
    // Просто проверяем что Spring context поднимается
    System.out.println("✅ Spring context loaded successfully!");
  }
}