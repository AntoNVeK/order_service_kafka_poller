package dev.kornienko.stub_service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/order-stub")
public class SubHttpRestController {

    private final StubProperties stubProperties;

    @PostMapping("/inventory/reserve")
    public ReserveInventoryResponseDto reserveInventory(
            @RequestParam String orderId
    ) {
        log.info("Reserve inventory for order called: orderId={}", orderId);
        StubUtils.randomSafeSleepMs(
                stubProperties.getInventoryLatencyMinMillis(),
                stubProperties.getInventoryLatencyMaxMillis()
        );
        if (stubProperties.isInventoryRejectEnabled()
                && StubUtils.chance(stubProperties.getInventoryRejectProbability())
        ) {
            log.info("Reserve inventory for order rejected: orderId={}", orderId);
            return new ReserveInventoryResponseDto(
                    orderId,
                    false,
                    "Very important cancellation reason for order=%s"
                            .formatted(orderId)
            );
        }

        return new ReserveInventoryResponseDto(
                orderId,
                true,
                null
        );
    }

    @PostMapping("/pricing")
    public PricingResponseDto calculatePricing(
            @RequestParam String orderId
    ) {
        log.info("Reserve calculate price for order called: orderId={}", orderId);
        StubUtils.randomSafeSleepMs(
                stubProperties.getPricingLatencyMinMillis(),
                stubProperties.getPricingLatencyMaxMillis()
        );
        if (stubProperties.isPricingExceptionEnabled()
                && StubUtils.chance(stubProperties.getPricingExceptionProbability())) {
            throw new RuntimeException("Unexpected server error while pricing calculation");
        }

        return new PricingResponseDto(
                orderId,
                BigDecimal.valueOf(
                        ThreadLocalRandom.current().nextInt(100, 10_000)
                )
        );
    }

    @PostMapping("/shipping/schedule")
    public ScheduleShippingResponseDto shipping(@RequestParam String orderId) {
        log.info("Reserve schedule shipping for order called: orderId={}", orderId);
        StubUtils.randomSafeSleepMs(
                stubProperties.getShippingLatencyMinMillis(),
                stubProperties.getShippingLatencyMaxMillis()
        );
        if (stubProperties.isShippingExceptionEnabled()
            && StubUtils.chance(stubProperties.getShippingExceptionProbability())) {
            throw new RuntimeException("Unexpected server error while shipping scheduling");
        }

        return new ScheduleShippingResponseDto(
                orderId,
                3
        );
    }

    public record ReserveInventoryResponseDto(
            String orderId,
            boolean stockReserved,
            String reserveCancellationReason // причина отказа (если stockReserved=true)
    ) {}

    public record PricingResponseDto(
            String orderId,
            BigDecimal price
    ) {}

    public record ScheduleShippingResponseDto(
            String orderId,
            int etaDeliveryDays // estimated time arrival
    ) {}
}
