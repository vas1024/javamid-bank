package javamid.exgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExgenApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExgenApplication.class, args);
	}

}
