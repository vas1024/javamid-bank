package javamid.accounts.controller;

import javamid.accounts.model.AuthRequest;
import javamid.accounts.service.UserService;
import javamid.accounts.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public List<User> getAllUsers(){
    return userService.findAll();
  }


  @GetMapping("/{id}")
  public ResponseEntity<User> getUser(@PathVariable Long id) {
      return userService.findById(id)
              .map(user -> ResponseEntity.ok()
                      .header("Custom-Header", "value")
                      .body(user))
              .orElse(ResponseEntity.notFound().build());
  }


  @PostMapping
  public ResponseEntity<?> createUser(@RequestBody User user) {

    Map<String, Object> result = userService.save(user);

    if ((Boolean) result.get("success")) {
      return ResponseEntity.created(URI.create("/api/users/" + ((User) result.get("user")).getId()))
              .body(result.get("user"));
    } else {
      System.out.println("createUser error: " + result.get("message"));
      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(result.get("message"));
    }
  }


  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {

    System.out.println( "UserController: delete user " + id );
    String result = userService.delete(id);
    System.out.println( result );
    if ("SUCCESS".equals(result)) return ResponseEntity.noContent().build(); // 204
    else return ResponseEntity.badRequest().body(result);
  }

  @PostMapping("/{id}/password")
  public ResponseEntity<Void> updatePassword(
          @PathVariable Long id,
          @RequestParam String newPassword) {

    userService.updatePassword(id, newPassword);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}")
  public ResponseEntity<Void> updateUser(
          @PathVariable Long id,
          @RequestBody Map<String, Object> updates) {

    logger.info("userController: updateUser: Received update for user {}: {}", id, updates);

    userService.updateUser(id, updates);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/login")
  public ResponseEntity<Long> postLogin(
          @RequestBody AuthRequest authRequest ) {

    String username = authRequest.getUsername();
    String password = authRequest.getPassword();
    logger.info("userController: request validate login for uname {}  pw {}", username, password );

    return ResponseEntity.ok( userService.validateLogin( username, password ) );

  }


}
