package com.pizzaflow.inventory.repository;

import com.pizzaflow.inventory.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

    Optional<Ingredient> findByName(String name);

    List<Ingredient> findByCategory(String category);

    List<Ingredient> findByIsActiveTrue();

    @Query("SELECT DISTINCT i.category FROM Ingredient i WHERE i.isActive = true ORDER BY i.category")
    List<String> findAllActiveCategories();
}
