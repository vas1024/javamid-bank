package javamid.front.model;

public class AuthResponse {
  private boolean success;
  private String accessToken;
  private String tokenType;
  private Long expiresIn;

  public void setSuccess( boolean success ) { this.success = success; }
  public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
  public void setTokenType(String tokenType) { this.tokenType = tokenType; }
  public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
  public boolean isSuccess() { return success; }
  public String getAccessToken() { return accessToken; }
  public String getTokenType() { return tokenType; }
  public Long getExpiresIn() { return expiresIn; }

  public AuthResponse(){}


}

