package dev.kornienko.order_processor_service.async;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface TaskEntityRepository extends JpaRepository<AsyncTaskEntity, Long> {

    @Query(value = """
                select * from tasks
                where (status = :newStatus)
                or (status = :retryStatus and next_attempt_at <= :now)
                or (status = :processingStatus and next_attempt_at <= :now)
                order by id
                limit :batchSize
                for update skip locked
            """, nativeQuery = true)
    List<AsyncTaskEntity> pickBatchForProcessing(
            @Param("newStatus") int newStatus,
            @Param("retryStatus") int retryStatus,
            @Param("processingStatus") int processingStatus,
            @Param("now") OffsetDateTime now,
            @Param("batchSize") int batchSize
    );

}
