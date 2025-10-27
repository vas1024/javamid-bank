package javamid.front.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import javamid.front.model.Currency4html;
import javamid.front.model.User;
import javamid.front.model.ExchangeRateDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Controller
public class FrontController {


  private final RestTemplate restTemplate;

  public FrontController(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  @GetMapping("/")
  public String getRoot(){
    return("redirect:/signup");
  }

/*
  @GetMapping("/{id}")
  public String getUserProfile(@PathVariable Long id, Model model) {
    User currentUser = new User();
    String errorMessage = "";
    try {
      // 1. Вызываем accounts сервис
      ResponseEntity<User> response = restTemplate.getForEntity(
              "http://gateway/accounts/api/users/{id}",
              User.class,
              id
      );
      if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
        currentUser = response.getBody();

        model.addAttribute("login", currentUser.getLogin());
        model.addAttribute("name", currentUser.getName());
        model.addAttribute("birthdate", currentUser.getBirthday()); // !! bithdate birthday
        System.out.println( "user birthday " + currentUser.getBirthday());

      }

      // 2. Вызываем currency сервис для списка валют
      ResponseEntity<List<ExchangeRateDto>> currencyResponse = restTemplate.exchange(
//          "http://127.0.0.1:8083/api/rates",
              "http://gateway/exchange/api/rates",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<ExchangeRateDto>>() {}
      );
      if ( currencyResponse.getStatusCode() == HttpStatus.OK && currencyResponse.hasBody()) {
        List<ExchangeRateDto> rates = currencyResponse.getBody();
        List<Currency4html> currencies = rates.stream()
                .map(rate -> new Currency4html(rate.getName(), rate.getTitle()))
                .collect(Collectors.toList());
        model.addAttribute("currency", currencies);
      }

      ResponseEntity<List<User>> allUsersResponse = restTemplate.exchange(
//              "http://localhost:8082/api/users",
              "http://gateway/accounts/api/users",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<User>>() {}
      );
      if ( allUsersResponse.getStatusCode() == HttpStatus.OK && allUsersResponse.hasBody()) {
        List<User> allUsers = allUsersResponse.getBody();
        final long currentUserId = currentUser.getId();
        List<User> otherUsers = allUsers.stream()
                .filter(user -> !user.getId().equals(currentUserId) )
                .collect(Collectors.toList());

        model.addAttribute("users", otherUsers);
      }

      return "main.html";

      } catch (Exception e) {
      // Обработка ошибок
      model.addAttribute("errors", "Err with accounts or exchange " + e.getMessage() );
      System.out.println("error with accounts or exchange service");
    }

    return "main.html";
  }
*/








  @GetMapping("/{id}")
  public String getUserProfile(@PathVariable Long id,
                               Model model,
                               HttpServletRequest request) {

    HttpEntity<String> entity = createAuthEntity(request);

    User currentUser = new User();
    String errorMessage = "";
    try {
      // 1. Вызываем accounts сервис
      ResponseEntity<User> response = restTemplate.exchange(
              "http://gateway/accounts/api/users/{id}",
              HttpMethod.GET,
              entity,
              User.class,
              id
      );


      if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
        currentUser = response.getBody();

        model.addAttribute("login", currentUser.getLogin());
        model.addAttribute("name", currentUser.getName());
        model.addAttribute("birthdate", currentUser.getBirthday()); // !! bithdate birthday
        System.out.println( "user birthday " + currentUser.getBirthday());

      }

      // 2. Вызываем currency сервис для списка валют
      ResponseEntity<List<ExchangeRateDto>> currencyResponse = restTemplate.exchange(
//          "http://127.0.0.1:8083/api/rates",
              "http://gateway/exchange/api/rates",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<ExchangeRateDto>>() {}
      );
      if ( currencyResponse.getStatusCode() == HttpStatus.OK && currencyResponse.hasBody()) {
        List<ExchangeRateDto> rates = currencyResponse.getBody();
        List<Currency4html> currencies = rates.stream()
                .map(rate -> new Currency4html(rate.getName(), rate.getTitle()))
                .collect(Collectors.toList());
        model.addAttribute("currency", currencies);
      }

      ResponseEntity<List<User>> allUsersResponse = restTemplate.exchange(
              "http://gateway/accounts/api/users",
              HttpMethod.GET,
              entity,
              new ParameterizedTypeReference<List<User>>() {}
      );
      if ( allUsersResponse.getStatusCode() == HttpStatus.OK && allUsersResponse.hasBody()) {
        List<User> allUsers = allUsersResponse.getBody();
        final long currentUserId = currentUser.getId();
        List<User> otherUsers = allUsers.stream()
                .filter(user -> !user.getId().equals(currentUserId) )
                .collect(Collectors.toList());

        model.addAttribute("users", otherUsers);
      }

      return "main.html";

    } catch (Exception e) {
      // Обработка ошибок
      model.addAttribute("errors", "Err with accounts or exchange " + e.getMessage() );
      System.out.println("error with accounts or exchange service");
    }

    return "main.html";
  }



  @GetMapping("/signup")
  public String getSignup(){
    return "signup.html";
  }

  @PostMapping("/signup")
  public String processSignup(
          @RequestParam String login,
          @RequestParam String password,
          @RequestParam("confirm_password") String confirmPassword,
          @RequestParam String name,
          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birthdate,
          Model model) {


    List<String> errors = new ArrayList<>();

    // Валидация
    if (!password.equals(confirmPassword)) {
      errors.add("Пароли не совпадают");
    }

    if (birthdate.isAfter(LocalDate.now().minusYears(18))) {
      errors.add("Возраст должен быть не менее 18 лет");
    }

    // Если есть ошибки - показываем форму снова
    if (!errors.isEmpty()) {
      model.addAttribute("errors", errors);
      model.addAttribute("login", login);
      model.addAttribute("name", name);
      model.addAttribute("birthdate", birthdate.toString());
      return "signup";
    }

    try {
      // Создаем DTO для отправки в account service
      User user = new User( login, password, name, birthdate);

      // Отправляем запрос в account service
      ResponseEntity<User> response = restTemplate.postForEntity(
//              "http://gateway/accounts/api/users",
              "http://accounts:8080/api/users",
              user,
              User.class
      );

      if (response.getStatusCode() == HttpStatus.CREATED) {
        // Редирект на страницу успеха
        User createdUser = response.getBody();
        return "redirect:/" + createdUser.getId();
      } else {
        errors.add("Ошибка при создании пользователя");
        model.addAttribute("errors", errors);
        return "signup";
      }

    } catch (Exception e) {
      errors.add("Пользователь с таким логином уже существует");
      model.addAttribute("errors", errors);
      model.addAttribute("login", login);
      model.addAttribute("name", name);
      model.addAttribute("birthdate", birthdate.toString());
      return "signup";
    }
  }




  @PostMapping("/user/{id}/editPassword")
  public String postEditPassword(
          @PathVariable Long id,
          @RequestParam String password,
          @RequestParam String confirm_password,
          Model model,
          RedirectAttributes redirectAttributes,
          HttpServletRequest request) {

    HttpEntity<String> entity = createAuthEntity(request);
    // Сохраняем все текущие атрибуты модели
    Map<String, Object> modelAttributes = model.asMap();
    modelAttributes.forEach(redirectAttributes::addFlashAttribute);

    // validation
    List<String> errors = new ArrayList<>();
    if (!password.equals(confirm_password)) {
      errors.add("Пароли не совпадают");
    }
    if (password.length() < 1 ) {
      errors.add("Пароль должен быть не менее 1 символов");
    }
    if (!errors.isEmpty()) {
      redirectAttributes.addFlashAttribute("errors", errors);
      return "redirect:/" + id;
    }

    try {
      /*
      restTemplate.postForObject(
              "http://gateway/accounts/api/users/{id}/password?newPassword={password}",
              null,
              Void.class,
              id, password
      );  */
      restTemplate.exchange(
              "http://gateway/accounts/api/users/{id}/password?newPassword={password}",
              HttpMethod.POST,
              entity,
              Void.class,
              Map.of("id", id, "password", password)
      );
      redirectAttributes.addFlashAttribute("message", "Пароль успешно изменен!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errors",
              List.of("Ошибка при изменении пароля: " + e.getMessage()));
    }

    return "redirect:/" + id;
  }



  @PostMapping("/user/{id}/editUserAccounts")
  public String postEditUserAccounts(
          @PathVariable Long id,
          @RequestParam String name,
          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birthdate,
          Model model,
          RedirectAttributes redirectAttributes,
          HttpServletRequest request) {


    HttpEntity<String> entity = createAuthEntity(request);
    //validation
    if ( birthdate != null && birthdate.isAfter(LocalDate.now().minusYears(18))) {
      redirectAttributes.addFlashAttribute("errors", "Возраст должен быть не менее 18 лет");
      return "redirect:/" + id ;
    }

    //filling map
    Map<String, Object> updates = new HashMap<>();
    if (name != null && !name.trim().isEmpty()) {
      updates.put("name", name);
    }
    if (birthdate != null) {
      updates.put("birthday", birthdate.toString()); // LocalDate сериализуется как список трех чисел
    }

    if(updates.size() == 0 ) {
      redirectAttributes.addFlashAttribute("errors", "Нечего менять");
      return "redirect:/" + id ;
    }

    try {
/*      restTemplate.postForObject(
              "http://gateway/accounts/api/users/{id}",
              updates,
              Void.class,
              id
      );*/
      HttpEntity<Map<String, Object>> entityWithBody = new HttpEntity<>(updates, entity.getHeaders());  // как по мне так это полный пипец
      restTemplate.exchange(
              "http://gateway/accounts/api/users/{id}",
              HttpMethod.POST,
              entityWithBody,
              Void.class,
              Map.of("id", id)
      );

      redirectAttributes.addFlashAttribute("message", "Данные успешно обновлены!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errors",
              List.of("Ошибка при обновлении данных: " + e.getMessage()));
    }

    return "redirect:/" + id ;
  }




  private String extractTokenFromRequest(HttpServletRequest request) {
      Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("jwt_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }

  private HttpEntity<String> createAuthEntity(HttpServletRequest request) {
    String token = extractTokenFromRequest(request);
    HttpHeaders headers = new HttpHeaders();
    if (token != null) {
      headers.set("Authorization", "Bearer " + token);
    } else {
      System.out.println("createAuthEntity: token = null");
    }
    return new HttpEntity<>(headers);
  }


}
