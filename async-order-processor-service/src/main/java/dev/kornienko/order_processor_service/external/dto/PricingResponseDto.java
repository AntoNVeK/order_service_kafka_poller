package dev.kornienko.order_processor_service.external.dto;

import java.math.BigDecimal;

public record PricingResponseDto(
            String orderId,
            BigDecimal price
) {}