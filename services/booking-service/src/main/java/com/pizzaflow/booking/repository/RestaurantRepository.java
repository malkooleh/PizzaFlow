package com.pizzaflow.booking.repository;

import com.pizzaflow.booking.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    List<Restaurant> findByIsActiveTrue();

    boolean existsByIdAndIsActiveTrue(UUID id);
}
