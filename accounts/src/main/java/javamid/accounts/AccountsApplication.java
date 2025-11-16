package javamid.accounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccountsApplication {

	public static void main(String[] args) {
		System.setProperty("service.name", "accounts");
		SpringApplication.run(AccountsApplication.class, args);
	}

}
