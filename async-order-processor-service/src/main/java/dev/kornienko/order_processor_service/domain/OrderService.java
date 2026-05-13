package dev.kornienko.order_processor_service.domain;

import dev.kornienko.order_processor_service.async.AsyncTaskEntity;
import dev.kornienko.order_processor_service.async.TaskEntityRepository;
import dev.kornienko.order_processor_service.async.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final TransactionTemplate txTemplate;
    private final TaskEntityRepository taskRepository;

    public OrderEntity createOrder(
            String address,
            String description,
            String email
    ) {
        log.info("Creating order with address {}, description {}, email {}", address, description, email);
        var entity = OrderEntity.builder()
                .status(OrderStatus.CREATION_PENDING)
                .address(address)
                .description(description)
                .email(email)
                .build();

        return txTemplate.execute(status -> {
            var created = orderRepository.save(entity);
            log.info("Created order id={}", created.getId());

            var task = AsyncTaskEntity.builder()
                    .orderId(created.getId())
                    .status(TaskStatus.NEW)
                    .build();

            var savedTask = taskRepository.save(task);
            log.info("Created task for order creation: taskId={}, orderId={}",
                    savedTask.getId(), created.getId());
            return created;
        });
    }
}
