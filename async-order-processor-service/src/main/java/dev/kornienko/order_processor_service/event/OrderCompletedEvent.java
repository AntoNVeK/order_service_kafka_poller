package dev.kornienko.order_processor_service.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCompletedEvent (
        UUID orderId,
        String email,
        String description,
        BigDecimal price,
        Integer etaDays
){}
