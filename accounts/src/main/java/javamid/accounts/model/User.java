package javamid.accounts.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(unique = true, nullable = false)
  private String login;
  @Column(nullable = false)
  private String password;
  private String name;
  private LocalDate birthday;


  public User(){};


  public void setId(Long id) { this.id = id; }
  public void setLogin(String login) { this.login = login; }
  public void setPassword(String password) { this.password = password; }
  public void setName(String name) { this.name = name; }
  public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
  public Long getId() { return id; }
  public String getLogin() { return login; }
  public String getPassword() { return password; }
  public String getName() { return name; }
  public LocalDate getBirthday() { return birthday; }
}
