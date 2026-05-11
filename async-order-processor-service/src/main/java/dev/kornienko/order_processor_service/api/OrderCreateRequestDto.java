package dev.kornienko.order_processor_service.api;

public record OrderCreateRequestDto(
        String description,
        String address,
        String email
) {}
