package dev.kornienko.order_processor_service.async;

import dev.kornienko.order_processor_service.async.properties.TaskDispatcherProperties;
import dev.kornienko.order_processor_service.domain.OrderRepository;
import dev.kornienko.order_processor_service.event.OrderCompletedEvent;
import dev.kornienko.order_processor_service.event.OrderFailedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@AllArgsConstructor
public class TaskDispatcher {

    private final ExecutorService taskDispatcherThreadPool;
    private final TaskEntityRepository taskRepository;
    private final TaskProcessor taskProcessor;
    private final TaskDispatcherProperties properties;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderRepository orderRepository;

    public void dispatch(AsyncTaskEntity task) {
        CompletableFuture
                .supplyAsync(() -> taskProcessor.processTask(task), taskDispatcherThreadPool)
                .thenAccept(result -> handleTaskExecuted(task, result))
                .exceptionally(ex ->  handleExceptionInTaskHappened(task, ex));
    }

    private Void handleExceptionInTaskHappened(
            AsyncTaskEntity task,
            Throwable ex
    ) {
        log.error("Task failed with unexpected exception: taskId={}", task.getId(), ex);
        scheduleTaskRetry(task);
        log.error("Success processed task failure: taskId={}", task.getId(), ex);
        return null;
    }

    private void handleTaskExecuted(
            AsyncTaskEntity task,
            TaskExecutionStatus taskExecutionStatus
    ) {
        log.info("Task executed: taskId={}, status={}", task.getId(), taskExecutionStatus);
        switch (taskExecutionStatus) {
            case SUCCESS -> {
                handleTaskSucceeded(task);
                sendOrderCompletedEvent(task);
            }
            case RETRYABLE_ERROR -> scheduleTaskRetry(task);
            case NON_RETRYABLE_ERROR -> {
                handleTaskFailed(task);
                sendOrderFailedEvent(task);
            }
        }
        log.info("Success processed execution status: taskId={}, status={}", task.getId(), taskExecutionStatus);
    }

    private void sendOrderCompletedEvent(AsyncTaskEntity task) {
        orderRepository.findById(task.getOrderId()).ifPresent(order -> {
            OrderCompletedEvent event = new OrderCompletedEvent(
                    order.getId(),
                    order.getEmail(),
                    order.getDescription(),
                    order.getPrice(),
                    order.getEtaDays()
            );
            sendKafkaEvent("order-completed", event);
        });
    }

    private void sendOrderFailedEvent(AsyncTaskEntity task) {
        orderRepository.findById(task.getOrderId()).ifPresent(order -> {
            OrderFailedEvent event = new OrderFailedEvent(
                    order.getId(),
                    order.getEmail(),
                    order.getDescription(),
                    order.getCancellationReason()
            );
            sendKafkaEvent("order-failed", event);
        });
    }


    private void sendKafkaEvent(String topic, Object event) {
        try {
            kafkaTemplate.send(topic, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Kafka event sent: topic={}, event={}", topic, event);
                        } else {
                            log.error("Failed to send Kafka event: topic={}", topic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending Kafka message", e);
        }
    }

    private void handleTaskFailed(AsyncTaskEntity task) {
        taskRepository.save(task.toBuilder()
                .status(TaskStatus.FAILED_NON_RETRYABLE)
                .nextAttemptAt(null)
                .build());
    }

    private void handleTaskSucceeded(AsyncTaskEntity task) {
        taskRepository.save(task.toBuilder()
                .status(TaskStatus.DONE)
                .nextAttemptAt(null)
                .build());
    }

    private void scheduleTaskRetry(AsyncTaskEntity task) {
        log.info("Scheduling default retry for taskId={}", task.getId());

        if (task.getAttempts() >= properties.getMaxAttempts()) {
            log.error("Maximum number of retries reached: taskId={}", task.getId());
            taskRepository.save(task.toBuilder()
                    .status(TaskStatus.FAILED_NON_RETRYABLE)
                    .nextAttemptAt(null)
                    .build());
            return;
        }

        var nextAttemptAt = OffsetDateTime.now().plus(properties.getRetryDelay());
        taskRepository.save(task.toBuilder()
                .status(TaskStatus.FAILED_RETRYABLE)
                .nextAttemptAt(nextAttemptAt)
                .build());
    }
}
