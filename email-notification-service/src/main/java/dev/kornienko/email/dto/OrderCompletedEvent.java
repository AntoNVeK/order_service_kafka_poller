package dev.kornienko.email.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCompletedEvent(
    UUID orderId,
    String email,
    String description,
    BigDecimal price,
    Integer etaDays
) {}