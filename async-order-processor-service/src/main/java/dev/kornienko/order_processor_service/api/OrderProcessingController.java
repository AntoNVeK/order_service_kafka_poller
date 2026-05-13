package dev.kornienko.order_processor_service.api;

import dev.kornienko.order_processor_service.domain.OrderEntity;
import dev.kornienko.order_processor_service.domain.OrderRepository;
import dev.kornienko.order_processor_service.domain.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderProcessingController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @PostMapping("/orders")
    public ResponseEntity<OrderInfoDto> createOrder(
            @RequestBody OrderCreateRequestDto requestDto
    ) {
        log.info("Received request to create order");
        OrderEntity entity = orderService.createOrder(requestDto.address(), requestDto.description(), requestDto.email());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapEntityToDto(entity));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderInfoDto> getOrderInfo(
            @PathVariable("id") UUID id
    ) {
        log.info("Received request to get order: id={}", id);
        OrderEntity order = orderRepository.findById(id)
                .orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapEntityToDto(order));
    }

    private OrderInfoDto mapEntityToDto(OrderEntity order) {
        return OrderInfoDto.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .address(order.getAddress())
                .description(order.getDescription())
                .reserved(order.getReserved())
                .price(order.getPrice())
                .etaDays(order.getEtaDays())
                .cancellationReason(order.getCancellationReason())
                .email(order.getEmail())
                .build();
    }

}
