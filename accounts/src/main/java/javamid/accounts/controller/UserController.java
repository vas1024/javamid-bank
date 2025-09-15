package javamid.accounts;

import javamid.accounts.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
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
  public ResponseEntity<User> createUser(@RequestBody User user) {
    User savedUser = userService.save(user).get();
    return ResponseEntity.created(URI.create("/api/users/" + savedUser.getId()))
            .body(savedUser); // 201
  }


  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build(); // 204
  }



}
