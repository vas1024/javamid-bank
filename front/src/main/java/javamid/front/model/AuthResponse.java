package javamid.front.model;

public class AuthResponse {
  private boolean success;
  private String accessToken;
  private String tokenType;
  private Long expiresIn;
  private Long userId;

  public void setSuccess( boolean success ) { this.success = success; }
  public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
  public void setTokenType(String tokenType) { this.tokenType = tokenType; }
  public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
  public void setUserId(Long userId ) { this.userId = userId; }
  public boolean isSuccess() { return success; }
  public String getAccessToken() { return accessToken; }
  public String getTokenType() { return tokenType; }
  public Long getExpiresIn() { return expiresIn; }
  public Long getUserId() { return  userId; }

  public AuthResponse(){}
  public AuthResponse( boolean success, String accessToken, String tokenType, Long expiresIn, Long userId ){
    this.success = success;
    this.accessToken = accessToken;
    this.tokenType = tokenType;
    this.expiresIn = expiresIn;
    this.userId = userId;
  }

}


