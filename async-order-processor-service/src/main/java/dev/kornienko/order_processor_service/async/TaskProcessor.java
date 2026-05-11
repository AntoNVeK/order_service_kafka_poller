package dev.kornienko.order_processor_service.async;

import dev.kornienko.order_processor_service.domain.OrderEntity;
import dev.kornienko.order_processor_service.domain.OrderRepository;
import dev.kornienko.order_processor_service.domain.OrderStatus;
import dev.kornienko.order_processor_service.event.OrderCompletedEvent;
import dev.kornienko.order_processor_service.external.StubHttpClient;
import dev.kornienko.order_processor_service.external.dto.PricingResponseDto;
import dev.kornienko.order_processor_service.external.dto.ReserveInventoryResponseDto;
import dev.kornienko.order_processor_service.external.dto.ScheduleShippingResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Component
@AllArgsConstructor
public class TaskProcessor {

    private final StubHttpClient stubHttpClient;
    private final OrderRepository orderRepository;
    private final ExecutorService externalHttpThreadPool;

    public TaskExecutionStatus processTask(AsyncTaskEntity task) {
        UUID orderId = task.getOrderId();

        var order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            log.error("Order not found: id={}", orderId);
            return TaskExecutionStatus.NON_RETRYABLE_ERROR;
        }

        var reservation = stubHttpClient.reserveInventory(orderId.toString());
        if (!reservation.stockReserved()) {
            return handleStockReservationRejected(order.get(), reservation);
        }

        var pricingFuture = CompletableFuture
                .supplyAsync(() -> stubHttpClient.calculatePricing(orderId.toString()), externalHttpThreadPool);

        var shippingFuture = CompletableFuture
                .supplyAsync(() -> stubHttpClient.scheduleShipping(orderId.toString()), externalHttpThreadPool);

        try {
            pricingFuture.thenAcceptBoth(
                    shippingFuture,
                    (pricing, shipping) -> handlePriceAndShippingSucceed(order.get(), pricing, shipping)
            ).get();
        } catch (InterruptedException e) {
            log.error("Thread was interrupted", e);
            Thread.currentThread().interrupt();
            return TaskExecutionStatus.RETRYABLE_ERROR;
        } catch (ExecutionException e) {
            log.error("Error while creating order: orderId={}, taskId={}", orderId, task.getId(), e);
            return TaskExecutionStatus.RETRYABLE_ERROR;
        }
        return TaskExecutionStatus.SUCCESS;
    }

    private void handlePriceAndShippingSucceed(
            OrderEntity order,
            PricingResponseDto pricing,
            ScheduleShippingResponseDto shipping
    ) {
        log.info("Order created successfully: orderId={}, price={}, shippingEta={}",
                order.getId(), pricing.price(), shipping.etaDeliveryDays());
        var orderToUpdate = order.toBuilder()
                .status(OrderStatus.CREATED_SUCCESSFULLY)
                .reserved(true)
                .price(pricing.price())
                .etaDays(shipping.etaDeliveryDays())
                .build();
        orderRepository.save(orderToUpdate);
    }

    private TaskExecutionStatus handleStockReservationRejected(
            OrderEntity order,
            ReserveInventoryResponseDto reservationResponse
    ) {
        log.info("Order reserved rejected: orderId={}, cancellationReason={}",
                order.getId(), reservationResponse.reserveCancellationReason());
        var orderToUpdate = order.toBuilder()
                .status(OrderStatus.CANCELLED)
                .reserved(false)
                .cancellationReason(reservationResponse.reserveCancellationReason())
                .build();
        orderRepository.save(orderToUpdate);
        return TaskExecutionStatus.SUCCESS;
    }

}
