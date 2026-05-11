package dev.kornienko.order_processor_service.external.dto;

public record ReserveInventoryResponseDto(
            String orderId,
            boolean stockReserved,
            String reserveCancellationReason // причина отказа (если stockReserved=true)
) {}