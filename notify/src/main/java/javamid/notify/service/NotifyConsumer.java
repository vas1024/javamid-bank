package javamid.notify.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;
/*
@Component
public class NotifyConsumer {

  private static final Logger log = LoggerFactory.getLogger(NotifyConsumer.class);


  @KafkaListener(topics = "notify")
  public void listen(String message) {
    log.info("Received notification: {}", message);
    // Ваша логика обработки уведомления
    processNotification(message);
  }

  @KafkaListener(topics = "transfer")
  public void consume(String message) {
    log.info("Received notification in transfer topic: {}", message);
    // Ваша логика обработки уведомления
    processNotification(message);
  }

 */


  private void processNotification(String message) {
    // Преобразуйте JSON в объект, обработайте и т.д.
    log.info("Processing notification: {}", message);
  }
}