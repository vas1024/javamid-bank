package javamid.transfer.client;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import javamid.transfer.model.TransferDto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Service
public class NotifyProducer  {

  private final KafkaTemplate<String, TransferDto> kafkaTemplate;
  private static final String TOPIC = "transfer";

  // Spring автоматически создаст KafkaTemplate на основе настроек в properties
  public NotifyProducer(KafkaTemplate<String, TransferDto> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  /*
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
*/



  public String notifyKafka(TransferDto transferDto) {
    try {
      CompletableFuture<SendResult<String, TransferDto>> future =
              kafkaTemplate.send("transfer-notifications", transferDto);

      // Ждем подтверждения с таймаутом
      SendResult<String, TransferDto> result = future.get(5, TimeUnit.SECONDS);

      // Проверяем, что сообщение действительно записалось
      if (result.getRecordMetadata() != null) {
        return "Notification sent successfully. Offset: " +
                result.getRecordMetadata().offset();
      } else {
        return "Error: No metadata received";
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return "Error: Operation interrupted";
    } catch (ExecutionException e) {
      return "Error sending to Kafka: " + e.getCause().getMessage();
    } catch (TimeoutException e) {
      return "Error: Kafka timeout - message may or may not have been delivered";
    }
  }



}

