package javamid.notify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@TestPropertySource(properties = {
				"spring.kafka.listener.auto-startup=false"     // Отключаем запуск listener'ов
})
class TemplateApplicationTests {


	@Test
	void contextLoads() {
	}

}
