package javamid.front.model;

public class UserDetails {
  private Long userId;
  private String username;

  public void setUserId(Long userId) { this.userId = userId; }
  public void setUsername(String username) { this.username = username; }
  public Long getUserId() { return userId; }
  public String getUsername() { return username; }

  public UserDetails(){}
  public UserDetails(Long userId, String username ){
    this.userId = userId;
    this.username = username;
  }
}