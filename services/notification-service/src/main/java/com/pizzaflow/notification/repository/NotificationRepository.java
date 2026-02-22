package com.pizzaflow.notification.repository;

import com.pizzaflow.notification.model.Notification;
import com.pizzaflow.notification.model.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Notification> findByUserIdAndStatus(UUID userId, NotificationStatus status);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);

    @Query("""
        SELECT n FROM Notification n 
        WHERE n.referenceId = :referenceId 
        AND n.referenceType = :referenceType
        ORDER BY n.createdAt DESC
        """)
    List<Notification> findByReference(
        @Param("referenceId") UUID referenceId,
        @Param("referenceType") String referenceType
    );

    @Modifying
    @Query("""
        DELETE FROM Notification n 
        WHERE n.createdAt < :cutoff 
        AND n.status IN ('DELIVERED', 'FAILED', 'READ')
        """)
    int deleteOldNotifications(@Param("cutoff") LocalDateTime cutoff);
}
