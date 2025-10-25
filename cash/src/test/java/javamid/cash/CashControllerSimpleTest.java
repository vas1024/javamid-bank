package javamid.cash;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc

public class CashControllerSimpleTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void testWithdrawal() throws Exception {
    String jsonRequest = """
            {
                "userId": 123,
                "currency": "USD",
                "value": 100.0
            }
            """;

    mockMvc.perform(post("/api/withdrawal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk());
  }

  @Test
  public void testDeposit() throws Exception {
    String jsonRequest = """
            {
                "userId": 456,
                "currency": "EUR",
                "value": 50.0
            }
            """;

    mockMvc.perform(post("/api/deposit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk());
  }
}