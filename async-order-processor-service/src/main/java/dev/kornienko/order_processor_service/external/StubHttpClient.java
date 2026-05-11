package dev.kornienko.order_processor_service.external;

import dev.kornienko.order_processor_service.external.dto.PricingResponseDto;
import dev.kornienko.order_processor_service.external.dto.ReserveInventoryResponseDto;
import dev.kornienko.order_processor_service.external.dto.ScheduleShippingResponseDto;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(accept = "application/json", contentType = "application/json")
public interface StubHttpClient {

    @PostExchange("/inventory/reserve")
    ReserveInventoryResponseDto reserveInventory(@RequestParam String orderId);

    @PostExchange("/pricing")
    PricingResponseDto calculatePricing(@RequestParam String orderId);

    @PostExchange("/shipping/schedule")
    ScheduleShippingResponseDto scheduleShipping(@RequestParam String orderId);
}
