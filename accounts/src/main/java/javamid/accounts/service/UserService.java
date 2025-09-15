package javamid.accounts;

import jakarta.transaction.Transactional;
import javamid.accounts.model.User;
import javamid.accounts.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
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


  public Optional<User> update(Long id, User userDetails) {
    return userRepository.findById(id)
            .map(user -> {
              user.setName(userDetails.getName());
              user.setBirthday(userDetails.getBirthday());
              user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
              return userRepository.save(user);
            });
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