package dev.kornienko.order_processor_service.event;

import java.util.UUID;

public record OrderFailedEvent(
        UUID orderId,
        String email,
        String description,
        String cancellationReason
) {}
