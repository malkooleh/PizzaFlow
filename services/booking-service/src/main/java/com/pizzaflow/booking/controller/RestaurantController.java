package com.pizzaflow.booking.controller;

import com.pizzaflow.booking.dto.RestaurantDTO;
import com.pizzaflow.booking.exception.RestaurantNotFoundException;
import com.pizzaflow.booking.repository.RestaurantRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
@Tag(name = "Restaurants", description = "Restaurant listing and details for the booking service")
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    public RestaurantController(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Operation(summary = "List all active restaurants")
    @GetMapping
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        List<RestaurantDTO> restaurants = restaurantRepository.findByIsActiveTrue().stream()
                .map(RestaurantDTO::from)
                .toList();
        return ResponseEntity.ok(restaurants);
    }

    @Operation(summary = "Get restaurant details by ID")
    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDTO> getRestaurant(@PathVariable UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .map(RestaurantDTO::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RestaurantNotFoundException(
                        "Restaurant not found: " + restaurantId));
    }
}
