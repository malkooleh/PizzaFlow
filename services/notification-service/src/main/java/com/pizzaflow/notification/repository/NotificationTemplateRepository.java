package com.pizzaflow.notification.repository;

import com.pizzaflow.notification.model.NotificationTemplate;
import com.pizzaflow.notification.model.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByName(String name);

    List<NotificationTemplate> findByChannelAndIsActiveTrue(NotificationChannel channel);

    List<NotificationTemplate> findByIsActiveTrue();
}
