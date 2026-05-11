package dev.kornienko.email.dto;

import java.util.UUID;

public record OrderFailedEvent(
    UUID orderId,
    String email,
    String description,
    String cancellationReason
) {}