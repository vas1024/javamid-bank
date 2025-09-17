package javamid.front.model;

import java.time.LocalDate;


public class User {


  private Long id;
  private String login;
  private String password;
  private String name;
  private LocalDate birthday;


  public User(){};

  public User(String login, String password, String name, LocalDate birthday) {
    this.login = login;
    this.password = password;
    this.name = name;
    this.birthday = birthday;
  }


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
