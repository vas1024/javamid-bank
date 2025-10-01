package javamid.front.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public class UserAuthDetails {
  private final Long userId;
  private final String username;

  public UserAuthDetails(Long userId, String username) {
    this.userId = userId;
    this.username = username;
  }

  public Long getUserId() { return userId; }
  public String getUsername() { return username; }

  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Override
  public String toString() {
    return "UserAuthDetails{userId=" + userId + ", username='" + username + "'}";
  }
}