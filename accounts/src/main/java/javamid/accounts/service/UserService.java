package javamid.accounts.service;

import jakarta.transaction.Transactional;
import javamid.accounts.model.User;
import javamid.accounts.repository.AccountRepository;
import javamid.accounts.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Map;

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



  public Optional<User> findById(Long id) {
    return userRepository.findById(id)
            .map( user -> {
              user.setPassword("***");
              return user;
            });
  }

  public Optional<User> findByLogin(String login) {
    return userRepository.findByLogin(login)
            .map(user -> {
              user.setPassword("***");
              return user;
            });
  }

  public boolean existsByLogin(String login) {
    return userRepository.existsByLogin(login);
  }


  /*
  public Optional<User> save(User user) {
    if (userRepository.existsByLogin(user.getLogin())) {
      return Optional.empty();
    }
    // Хешируем пароль перед сохранением
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return Optional.ofNullable( userRepository.save(user) )
            .map( savedUser -> {
              savedUser.setPassword("***");
              return savedUser;
            });
  }
  */

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
      savedUser.setPassword("***");
      return Map.of(
              "success", true,
              "message", "Пользователь успешно создан",
              "user", savedUser
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
              user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
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


  public void delete(Long id) {

    userRepository.deleteById(id);
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


}