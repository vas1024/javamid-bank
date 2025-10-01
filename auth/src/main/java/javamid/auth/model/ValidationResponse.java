package javamid.auth.model;

public class ValidationResponse {
  private boolean valid;
  private String username;
  private Long userId;

  public void setValid(boolean valid) { this.valid = valid; }
  public void setUsername(String username) { this.username = username; }
  public void setUserId(Long userId) { this.userId = userId;  }
  public boolean isValid() { return valid; }
  public String getUsername() { return username; }
  public Long getUserId() { return userId; }
}