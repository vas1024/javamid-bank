package javamid.accounts.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import javamid.accounts.model.User;
import javamid.accounts.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void createUser_Success() throws Exception {
    // Arrange - подготовка данных
    User user = new User();
    user.setLogin("testuser");
    user.setPassword("password123");
    user.setName("Test User");
    user.setBirthday(LocalDate.of(1990, 1, 1));

    User savedUser = new User();
    savedUser.setId(1L);
    savedUser.setLogin("testuser");
    savedUser.setName("Test User");
    savedUser.setBirthday(LocalDate.of(1990, 1, 1));

    // Мокируем сервис
    when(userService.save(any(User.class))).thenReturn(Map.of(
            "success", true,
            "message", "Пользователь успешно создан",
            "user", savedUser
    ));

    // Act & Assert - выполняем запрос и проверяем результат
    mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Пользователь успешно создан"))
            .andExpect(jsonPath("$.user.id").value(1L))
            .andExpect(jsonPath("$.user.login").value("testuser"));
  }

  @Test
  void createUser_AlreadyExists() throws Exception {
    // Arrange
    User user = new User();
    user.setLogin("existinguser");
    user.setPassword("password123");

    when(userService.save(any(User.class))).thenReturn(Map.of(
            "success", false,
            "message", "Пользователь с логином 'existinguser' уже существует"
    ));

    // Act & Assert
    mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk()) // или isBadRequest() в зависимости от логики
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Пользователь с логином 'existinguser' уже существует"));
  }
}