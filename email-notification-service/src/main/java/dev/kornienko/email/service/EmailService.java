package dev.kornienko.email.service;

import dev.kornienko.email.dto.OrderCompletedEvent;
import dev.kornienko.email.dto.OrderFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSuccessEmail(OrderCompletedEvent event) {
        String text = String.format("""
                Hello!
                
                Your order %s has been successfully completed!
                
                Order details:
                - Description: %s
                - Total amount: $%.2f
                - Delivery ETA: %d days
                
                Thank you for your purchase!
                
                Best regards,
                Order Processing Team
                """,
                event.orderId(),
                event.description(),
                event.price(),
                event.etaDays()
        );
        sendEmail(event.email(), "Order Completed", text);
    }

    public void sendFailureEmail(OrderFailedEvent event) {
        String text = String.format("""
                Hello!
                
                Unfortunately, your order %s could not be completed.
                
                Order details:
                - Description: %s
                - Reason: %s
                
                Please try again later or contact support.
                
                Best regards,
                Order Processing Team
                """,
                event.orderId(),
                event.description(),
                event.cancellationReason()
        );
        sendEmail(event.email(), "Order Failed", text);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
}