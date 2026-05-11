package dev.kornienko.order_processor_service.external.dto;

public record ScheduleShippingResponseDto(
            String orderId,
            int etaDeliveryDays // estimated time arrival
) {}