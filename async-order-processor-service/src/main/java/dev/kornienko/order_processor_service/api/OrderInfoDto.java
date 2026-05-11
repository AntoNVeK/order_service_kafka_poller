package dev.kornienko.order_processor_service.api;

import dev.kornienko.order_processor_service.domain.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class OrderInfoDto {
    UUID orderId;
    OrderStatus status;
    String address;
    String description;
    String email;
    Boolean reserved; // флаг брони
    BigDecimal price; // финальная цена заказа, если успешно зарезервирован
    Integer etaDays;  // ожидаемое время доставка, если успешно зарезервирован
    String cancellationReason; // причина отмены бронирования
}
