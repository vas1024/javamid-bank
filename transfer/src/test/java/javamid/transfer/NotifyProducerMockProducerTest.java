package javamid.transfer;


import javamid.transfer.client.NotifyProducer;
import javamid.transfer.model.TransferDto;
import org.apache.kafka.clients.producer.Producer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class NotifyProducerMockProducerTest {

  @Test
  void shouldSendMessageToKafka() throws Exception {
    // 1. Создаем MockProducer
    MockProducer<String, TransferDto> mockProducer =
            new MockProducer<>(true, new StringSerializer(), new JsonSerializer<>());

    // 2. Создаем KafkaTemplate с анонимным классом
    KafkaTemplate<String, TransferDto> kafkaTemplate = new KafkaTemplate<>(
            new DefaultKafkaProducerFactory<String, TransferDto>(Collections.emptyMap()) {
              @Override
              public Producer<String, TransferDto> createProducer() {
                return mockProducer;
              }
            }
    );

    // 3. Создаем продюсер
    NotifyProducer notifyProducer = new NotifyProducer(kafkaTemplate);

    // 4. Тестовые данные
    TransferDto transferDto = new TransferDto(123L, new BigDecimal("100.50"), "USD",
            456L, new BigDecimal("95.25"), "EUR");

    // 5. Вызываем метод
    String result = notifyProducer.notifyKafka(transferDto);

    // 6. Проверяем
    assertThat(result).contains("Notification sent successfully. Offset: 0");
    assertThat(mockProducer.history()).hasSize(1);
    assertThat(mockProducer.history().get(0).topic()).isEqualTo("transfer");
  }
}