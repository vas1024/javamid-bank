package javamid.notify.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "users")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String message;
  private Boolean read;


  public void setId(Long id) { this.id = id; }
  public void setMessage(String message) { this.message = message; }
  public void setRead(Boolean read) { this.read = read; }
  public Long getId() { return id; }
  public String getMessage() { return message; }
  public Boolean getRead() { return read; }

}