package dev.kornienko.email.consumer;

import dev.kornienko.email.dto.OrderCompletedEvent;
import dev.kornienko.email.dto.OrderFailedEvent;
import dev.kornienko.email.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderConsumer {

    private final EmailService emailService;

     public OrderConsumer(EmailService emailService) {
         this.emailService = emailService;
     }

    @KafkaListener(
            topics = "order-completed",
            containerFactory = "completedFactory"
    )
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("Completed: {}", event.orderId());
        emailService.sendSuccessEmail(event);
    }

    @KafkaListener(
            topics = "order-failed",
            containerFactory = "failedFactory"
    )
    public void handleOrderFailed(OrderFailedEvent event) {
        log.info("Failed: {}", event.orderId());
        emailService.sendFailureEmail(event);
    }
}
