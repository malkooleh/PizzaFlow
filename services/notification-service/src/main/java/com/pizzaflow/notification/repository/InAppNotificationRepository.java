package com.pizzaflow.notification.repository;

import com.pizzaflow.notification.model.InAppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InAppNotificationRepository extends JpaRepository<InAppNotification, UUID> {

    Page<InAppNotification> findByUserIdAndIsArchivedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<InAppNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    @Query("""
        SELECT COUNT(n) FROM InAppNotification n 
        WHERE n.userId = :userId 
        AND n.isRead = false 
        AND n.isArchived = false
        """)
    int countUnreadByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("""
        UPDATE InAppNotification n 
        SET n.isRead = true 
        WHERE n.userId = :userId 
        AND n.isRead = false
        """)
    int markAllAsRead(@Param("userId") UUID userId);

    @Modifying
    @Query("""
        UPDATE InAppNotification n 
        SET n.isArchived = true 
        WHERE n.userId = :userId 
        AND n.id IN :ids
        """)
    int archiveNotifications(@Param("userId") UUID userId, @Param("ids") List<UUID> ids);
}
