package javamid.accounts.service;

import jakarta.transaction.Transactional;
import javamid.accounts.model.Account;
import javamid.accounts.model.User;
import javamid.accounts.repository.AccountRepository;
import javamid.accounts.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository,
                     AccountRepository accountRepository,
                     PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;
  }


  private User createSafeUser(User original) {
    User safeUser = new User();
    safeUser.setId(original.getId());
    safeUser.setLogin(original.getLogin());
    safeUser.setName(original.getName());
    safeUser.setBirthday(original.getBirthday());
    safeUser.setPassword("***"); // маскируем
    return safeUser;
  }

  public Optional<User> findById(Long id) {
    return userRepository.findById(id)
            .map(this::createSafeUser);
  }

  // антипаттерн на заметку
  /*  так делать нельзя!!! меняет пароль, так как user - entity
  public Optional<User> findById(Long id) {
    return userRepository.findById(id)
            .map( user -> {
              user.setPassword("***");
              return user;
            });
  }*/


  public Optional<User> findByLogin(String login) {
    return userRepository.findByLogin(login)
            .map(this::createSafeUser);
  }

  public boolean existsByLogin(String login) {
    return userRepository.existsByLogin(login);
  }



  public Map<String, Object> save(User user) {
    if (userRepository.existsByLogin(user.getLogin())) {
      return Map.of(
              "success", false,
              "message", "Пользователь с логином '" + user.getLogin() + "' уже существует"
      );
    }
    try {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
      User savedUser = userRepository.save(user);
      System.out.println( "UserService: just created user " + savedUser.getLogin() + " with password " + savedUser.getPassword() );
      //если тут менять пароль, спринг будет его сохранять, так как savedUser - это entity!!!
      //savedUser.setPassword("***");
      // Создаем простой POJO для ответа (не entity)
      User responseUser = new User();
      responseUser.setId(savedUser.getId());
      responseUser.setLogin(savedUser.getLogin());
      responseUser.setName(savedUser.getName());
      responseUser.setBirthday(savedUser.getBirthday());
      responseUser.setPassword("***"); // безопасно - это не entity

      return Map.of(
              "success", true,
              "message", "Пользователь успешно создан",
              "user", responseUser
      );
    } catch (Exception e) {
      return Map.of(
              "success", false,
              "message", "Ошибка при сохранении пользователя: " + e.getMessage()
      );
    }
  }


  public Optional<User> update(Long id, User userDetails) {
    return userRepository.findById(id)
            .map(user -> {
              user.setName(userDetails.getName());
              user.setBirthday(userDetails.getBirthday());
              return userRepository.save(user);
            });
  }


  public void updatePassword(Long id, String newPassword) {
     User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    String encodedPassword = passwordEncoder.encode(newPassword);
    user.setPassword(encodedPassword);
    userRepository.save(user);
  }

  public void updateUser(Long id, Map<String, Object> updates) {

    User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (updates.containsKey("name")) {
      user.setName((String) updates.get("name"));
    }
    if (updates.containsKey("birthday")) {
      user.setBirthday( LocalDate.parse( (String) updates.get("birthday")) ); // издержки сериализации
    }

    userRepository.save(user);
  }


  public String delete(Long userId) {
    if (!userRepository.existsById( userId )) {
      return "Пользователь не найден";
    }

    List<Account> accounts = accountRepository.findByUserId(userId);
    List<String> currencies = accounts.stream()
            .filter( account -> account.getBalance().compareTo(BigDecimal.ZERO) > 0 )
            .map(Account::getCurrency)
            .collect(Collectors.toList());
    if ( ! currencies.isEmpty() )  return "у пользователя есть ненулевые счета: " + String.join(", ", currencies);

    userRepository.deleteById(userId);
    return "SUCCESS";
  }



  public List<User> findAll() {
    return userRepository.findAll();
  }

  public List<User> findByName(String name) {
    return userRepository.findByNameContaining(name);
  }

  public boolean validatePassword(String rawPassword, String encodedPassword) {
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }

  public Long validateLogin(String username, String password) {
    Optional<User> userOptional = userRepository.findByLogin(username);
    if (userOptional.isPresent()) {
      User user = userOptional.get();
      System.out.println("UserService saved password hash is " + user.getPassword());
      if (passwordEncoder.matches(password, user.getPassword())) return user.getId();
      else return -1L;
    } else return -1L;
  }

}