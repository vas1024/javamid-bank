package javamid.auth.model;



public class Notification {


  private Long id;
  private Long userId;
  private String message;
  private Boolean read;


  public void setId(Long id) { this.id = id; }
  public void setUserId(Long userId ) { this.userId = userId; }
  public void setMessage(String message) { this.message = message; }
  public void setRead(Boolean read) { this.read = read; }
  public Long getId() { return id; }
  public Long getUserId() { return  userId; }
  public String getMessage() { return message; }
  public Boolean getRead() { return read; }

  public Notification(){};
  public Notification( Long userId, String message ) {
    this.userId = userId;
    this.message = message;
    this.read = false;
  }

}