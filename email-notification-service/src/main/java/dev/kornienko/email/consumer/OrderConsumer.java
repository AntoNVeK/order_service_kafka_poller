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

    @KafkaListener(topics = "order-completed", groupId = "email-group")
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("Received OrderCompletedEvent: orderId={}, email={}", 
            event.orderId(), event.email());
        emailService.sendSuccessEmail(event);
    }

    @KafkaListener(topics = "order-failed", groupId = "email-group")
    public void handleOrderFailed(OrderFailedEvent event) {
        log.info("Received OrderFailedEvent: orderId={}, email={}, reason={}", 
            event.orderId(), event.email(), event.cancellationReason());
        emailService.sendFailureEmail(event);
    }
}