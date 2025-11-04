package javamid.notify.controller;

import javamid.notify.model.TransferDto;
import javamid.notify.service.NotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotifyConsumer {

  private final NotifyService notifyService;
  private static final Logger log = LoggerFactory.getLogger(NotifyConsumer.class);
  public NotifyConsumer( NotifyService notifyService){this.notifyService=notifyService;}

  @KafkaListener(topics = "transfer")
  public void consume(TransferDto transferDto) {
    log.info("KAFKA LISTENER TRIGGERED!");
    log.info("Received transfer: {}", transferDto);

    notifyService.saveTransferNote( transferDto );
  }


}