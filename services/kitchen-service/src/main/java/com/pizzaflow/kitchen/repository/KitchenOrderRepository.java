package com.pizzaflow.kitchen.repository;

import com.pizzaflow.kitchen.model.KitchenOrder;
import com.pizzaflow.kitchen.model.enums.KitchenOrderStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KitchenOrderRepository extends CrudRepository<KitchenOrder, String> {

    Optional<KitchenOrder> findByOrderId(Long orderId);

    List<KitchenOrder> findByRestaurantId(Long restaurantId);

    List<KitchenOrder> findByRestaurantIdAndStatus(Long restaurantId, KitchenOrderStatus status);

    List<KitchenOrder> findByRestaurantIdAndStatusIn(Long restaurantId, List<KitchenOrderStatus> statuses);

    boolean existsByOrderId(Long orderId);
}
