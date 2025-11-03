package javamid.notify.service;

import javamid.notify.model.TransferDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransferConsumer {

  private static final Logger log = LoggerFactory.getLogger(TransferConsumer.class);

  @KafkaListener(topics = "transfer")
  public void listen(TransferDto transfer) {
    log.info("Received transfer: {}", transfer);

    // Ваша логика обработки трансфера
    processTransfer(transfer);
  }

  private void processTransfer(TransferDto transfer) {
    log.info("Processing transfer from user {} to user {}",
            transfer.getUserIdFrom(), transfer.getUserIdTo());
    // Бизнес-логика уведомлений
  }
}