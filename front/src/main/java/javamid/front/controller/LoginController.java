package javamid.front.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javamid.front.model.AuthRequest;
import javamid.front.model.AuthResponse;
import javamid.front.util.AuthUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class LoginController {

  private final RestTemplate restTemplate;
  private final AuthUtils authUtils;
  public LoginController(RestTemplate restTemplate,
                         AuthUtils authUtils) {
    this.restTemplate = restTemplate;
    this.authUtils = authUtils;
  }



  @GetMapping("/login")
  public String loginForm(@RequestParam(value = "error", required = false) String error,
                          Model model) {
    if (error != null) {
      model.addAttribute("error", "Неверный логин или пароль");
    }
    return "login";
  }



  @PostMapping("/login")
  public String processLogin(@RequestParam String username,
                             @RequestParam String password,
                             HttpServletResponse response) {

    System.out.println("login username: " + username + "  pw: " + password );
    try {
      // Вызываем Auth Service для получения JWT
      ResponseEntity<AuthResponse> authResponse = restTemplate.postForEntity(
              "http://gateway/auth/api/login",
              new AuthRequest(username, password),
              AuthResponse.class
      );

      if (authResponse.getStatusCode().is2xxSuccessful() &&
              authResponse.getBody().isSuccess()) {

        String token = authResponse.getBody().getAccessToken();
        Long userId = authResponse.getBody().getUserId();
        System.out.println("LoginController : получили токен " + token );
        System.out.println("userId " + userId );



        // Сохраняем JWT в cookie (httpOnly для безопасности)
        Cookie authCookie = new Cookie("jwt_token", token);
        authCookie.setHttpOnly(true);
        authCookie.setPath("/");
        authCookie.setMaxAge(3600); // 1 час
        response.addCookie(authCookie);

        return "redirect:/" + userId;
      }
    } catch (Exception e) {
      // Логируем ошибку
    }

    return "redirect:/login?error";
  }





  @PostMapping("/logout")
  public String logout(HttpServletResponse response) {
    // Удаляем JWT cookie
    Cookie authCookie = new Cookie("jwt_token", "");
    authCookie.setHttpOnly(true);
    authCookie.setPath("/");
    authCookie.setMaxAge(0);
    response.addCookie(authCookie);

    return "redirect:/login?logout";
  }






  @GetMapping("/user/{id}/delete")
  public String getDelete(@PathVariable Long id,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          HttpServletRequest request){

    System.out.println("LoginController: Delete user: " + id );

    HttpEntity<String> entity = authUtils.createAuthEntity(request);

    try {
/*       restTemplate.delete(
              "http://gateway/accounts/api/users/{id}",
               id,
               Void.class
      );*/
      restTemplate.exchange(
              "http://gateway/accounts/api/users/{id}",
              HttpMethod.DELETE,
              entity,
              Void.class,
              Map.of("id", id)
      );
      {
        return "redirect:/" ;
      }
    } catch (HttpClientErrorException e) {
      String message = e.getResponseBodyAsString();
      redirectAttributes.addFlashAttribute("errors", message);
      return "redirect:/" + id ;
    }


  }

}