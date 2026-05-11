package dev.kornienko.order_processor_service.async;

import dev.kornienko.order_processor_service.async.properties.TaskPollerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Component
public class TaskPoller {

    private final TaskEntityRepository taskRepository;
    private final TransactionTemplate txTemplate;
    private final TaskDispatcher taskDispatcher;
    private final TaskPollerProperties taskPollerProperties;

    @Scheduled(fixedDelayString = "${task-execution.poller.poll-interval-ms}")
    public void poll() {
        log.info("Starting polling tasks");
        List<AsyncTaskEntity> tasksBatch = pickTasksForProcessing();

        var tasksIds = tasksBatch.stream()
                .map(AsyncTaskEntity::getId)
                .toList();
        log.info("Successfully picked tasks: count={}, ids={}",
                tasksIds.size(), tasksIds);

        if (tasksBatch.isEmpty()) {
            return;
        }
        for (AsyncTaskEntity task : tasksBatch) {
            taskDispatcher.dispatch(task);
        }
    }

    private List<AsyncTaskEntity> pickTasksForProcessing() {
        return txTemplate.execute(status ->  {
            List<AsyncTaskEntity> tasks = taskRepository.pickBatchForProcessing(
                    TaskStatus.NEW.getCode(),
                    TaskStatus.FAILED_RETRYABLE.getCode(),
                    TaskStatus.IN_PROGRESS.getCode(),
                    OffsetDateTime.now(),
                    taskPollerProperties.getBatchSize()
            );
            var nextProcessTime = OffsetDateTime.now()
                    .plus(taskPollerProperties.getRetryDelay());
            for (AsyncTaskEntity task : tasks) {
                task.setStatus(TaskStatus.IN_PROGRESS);
                var attempts = task.getAttempts() == null
                        ? 1 : task.getAttempts() + 1;
                task.setAttempts(attempts);
                task.setNextAttemptAt(nextProcessTime);
            }
            taskRepository.saveAll(tasks);
            return tasks;
        });

    }

}
