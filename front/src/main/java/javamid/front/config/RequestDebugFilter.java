package javamid.front.config;

import org.springframework.stereotype.Component;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.stereotype.Component;

// @Component
public class RequestDebugFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String path = httpRequest.getServletPath();
    String method = httpRequest.getMethod();

    System.out.println("=== REQUEST DEBUG ===");
    System.out.println("METHOD: " + method);
    System.out.println("PATH: " + path);
    System.out.println("QUERY: " + httpRequest.getQueryString());
    System.out.println("=====================");

    chain.doFilter(request, response);
  }
}
