package javamid.transfer.client;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import javamid.transfer.model.TransferDto;

import java.util.concurrent.ExecutionException;


@Service
public class NotifyProducer  {

  private final KafkaTemplate<String, TransferDto> kafkaTemplate;
  private static final String TOPIC = "transfer";

  // Spring автоматически создаст KafkaTemplate на основе настроек в properties
  public NotifyProducer(KafkaTemplate<String, TransferDto> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public String notifyKafka(TransferDto transferDto) {
    try {
      // Отправляем сообщение в Kafka
      SendResult<String, TransferDto> result = kafkaTemplate.send(TOPIC, transferDto).get();
      return "Notification sent successfully to Kafka. Offset: " +
              result.getRecordMetadata().offset();

    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      return "Error sending to Kafka: " + e.getMessage();
    }
  }
}

