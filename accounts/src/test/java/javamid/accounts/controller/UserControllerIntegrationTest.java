package javamid.accounts.controller;


import javamid.accounts.model.User;
import javamid.accounts.repository.UserRepository;
import javamid.accounts.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")  // Используем test профиль
@Transactional           // Откатываем транзакции после каждого теста
class UserControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ObjectMapper objectMapper;

  /*
  @Test
  void createUser_IntegrationTest() throws Exception {
    // Arrange - подготавливаем данные
    User user = new User();
    user.setLogin("integrationuser");
    user.setPassword("rawpassword123");
    user.setName("Integration Test User");
    user.setBirthday(LocalDate.of(1990, 5, 15));

    String userJson = objectMapper.writeValueAsString(user);

    // Act - выполняем запрос
    mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Пользователь успешно создан"))
            .andExpect(jsonPath("$.user.login").value("integrationuser"))
            .andExpect(jsonPath("$.user.name").value("Integration Test User"));

    // Assert - проверяем, что пользователь действительно сохранен в БД
    User savedUser = userRepository.findByLogin("integrationuser").orElse(null);

    assertThat(savedUser).isNotNull();
    assertThat(savedUser.getLogin()).isEqualTo("integrationuser");
    assertThat(savedUser.getName()).isEqualTo("Integration Test User");
    assertThat(savedUser.getBirthday()).isEqualTo(LocalDate.of(1990, 5, 15));

    // Проверяем, что пароль захеширован (не равен исходному)
    assertThat(savedUser.getPassword()).isNotEqualTo("rawpassword123");
    assertThat(savedUser.getPassword()).startsWith("$2a$"); // BCrypt формат

    System.out.println("=== ТЕСТ УСПЕШЕН ===");
    System.out.println("Создан пользователь: " + savedUser.getLogin());
    System.out.println("Хэш пароля: " + savedUser.getPassword());
    System.out.println("ID пользователя: " + savedUser.getId());
  }

  @Test
  void createUser_DuplicateLogin_ReturnsError() throws Exception {
    // Arrange - сначала создаем пользователя
    User firstUser = new User();
    firstUser.setLogin("duplicateuser");
    firstUser.setPassword("pass123");
    firstUser.setName("First User");
    userService.save(firstUser);

    // Пытаемся создать пользователя с таким же логином
    User duplicateUser = new User();
    duplicateUser.setLogin("duplicateuser"); // тот же логин
    duplicateUser.setPassword("anotherpass");
    duplicateUser.setName("Duplicate User");

    String duplicateUserJson = objectMapper.writeValueAsString(duplicateUser);

    // Act & Assert
    mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(duplicateUserJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Пользователь с логином 'duplicateuser' уже существует"));

    // Проверяем, что в БД только один пользователь с этим логином
    long userCount = userRepository.findAll().stream()
            .filter(u -> u.getLogin().equals("duplicateuser"))
            .count();
    assertThat(userCount).isEqualTo(1);
  }

   */
}