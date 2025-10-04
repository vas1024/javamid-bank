package javamid.exgen;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@SpringBootTest
class TemplateApplicationTests {

// чтобы успешно поднялся контекст в это мс надо чтобы были запущены
// config, eureka, auth, gateway, и, возможно, еще exchange
/*
	@Test
	void contextLoads() {
	}
*/

}
