package javamid.notify;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import javamid.notify.model.TransferDto;
import javamid.notify.model.Notification;
import javamid.notify.repository.NotifyRepository;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;




@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"transfer"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=test-group",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        // Сериализаторы для продюсера
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "spring.kafka.producer.properties.spring.json.add.type.headers=false",
        // Десериализаторы для консьюмера
        "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
        "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=*",
        "spring.kafka.consumer.properties.spring.json.value.default.type=javamid.notify.model.TransferDto",
        "spring.kafka.consumer.properties.spring.json.use.type.headers=false"
})
class KafkaConsumerTest {

  @Autowired
  private KafkaTemplate<String, TransferDto> kafkaTemplate;

  @MockBean
  private NotifyRepository notifyRepository;


  @Autowired
  private EmbeddedKafkaBroker embeddedKafka;
  @AfterEach
  void tearDown() throws Exception {
    // Даем время на завершение обработки
    Thread.sleep(1000);
    embeddedKafka.destroy();
  }

  @Test
  void shouldSaveCorrectNotification() throws Exception {
    // Given
    TransferDto transferDto = new TransferDto(123L, new BigDecimal("100.50"), "USD",
            456L, new BigDecimal("95.25"), "EUR");

    // When
    SendResult<String, TransferDto> sendResult = kafkaTemplate.send("transfer", transferDto).get();
    System.out.println("Message sent to offset: " + sendResult.getRecordMetadata().offset());

    // Даем больше времени на обработку
    Thread.sleep(1000);

    // Then
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notifyRepository, timeout(10000)).save(captor.capture());

    Notification saved = captor.getValue();
    assertThat(saved.getUserId()).isEqualTo(456L); // userIdTo
    assertThat(saved.getMessage()).isEqualTo("Пользователь 123 перевел вам 95.25 EUR.");
    assertThat(saved.getRead()).isFalse();
  }


}




