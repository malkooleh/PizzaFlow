package com.pizzaflow.catalog.repository;

import com.pizzaflow.catalog.model.MenuItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends MongoRepository<MenuItem, String> {

    List<MenuItem> findByRestaurantId(String restaurantId);

    List<MenuItem> findByRestaurantIdAndAvailable(String restaurantId, boolean available);

    List<MenuItem> findByRestaurantIdAndCategory(String restaurantId, String category);

    List<MenuItem> findByRestaurantIdAndFeaturedTrue(String restaurantId);

    @Query("{ 'restaurantId': ?0, 'name': { $regex: ?1, $options: 'i' } }")
    List<MenuItem> searchByName(String restaurantId, String namePattern);

    List<MenuItem> findByRestaurantIdAndDietaryTagsContaining(String restaurantId, String dietaryTag);
}
