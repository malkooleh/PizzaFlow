package com.pizzaflow.catalog.config;

import com.pizzaflow.catalog.model.MenuItem;
import com.pizzaflow.catalog.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Data initializer for populating sample menu items.
 * Only runs when spring.data.mongodb.init-data=true (enabled by default in
 * dev).
 * Disable in production by setting spring.data.mongodb.init-data=false
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.data.mongodb.init-data", havingValue = "true", matchIfMissing = true // Enabled by
                                                                                                           // default
)
public class DataInitializer implements ApplicationRunner {

    private final MenuItemRepository menuItemRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Checking if sample data initialization is needed...");

        // Only initialize if database is empty
        if (menuItemRepository.count() > 0) {
            log.info("Database already contains {} items. Skipping initialization.",
                    menuItemRepository.count());
            return;
        }

        log.info("Initializing sample menu data...");
        initializeSampleData();
        log.info("Sample data initialization completed. Total items: {}",
                menuItemRepository.count());
    }

    private void initializeSampleData() {
        String restaurantId = "RESTAURANT-001";

        // Pizzas
        MenuItem margherita = createPizza(
                restaurantId,
                "Margherita",
                "Classic pizza with tomato sauce, mozzarella, and basil",
                new BigDecimal("12.99"),
                true,
                true,
                List.of("GLUTEN", "DAIRY"),
                List.of("VEGETARIAN"));

        MenuItem pepperoni = createPizza(
                restaurantId,
                "Pepperoni",
                "Tomato sauce, mozzarella, and spicy pepperoni",
                new BigDecimal("14.99"),
                true,
                true,
                List.of("GLUTEN", "DAIRY"),
                List.of());

        MenuItem veggiePizza = createPizza(
                restaurantId,
                "Veggie Supreme",
                "Loaded with bell peppers, onions, mushrooms, olives, and tomatoes",
                new BigDecimal("13.99"),
                true,
                false,
                List.of("GLUTEN", "DAIRY"),
                List.of("VEGETARIAN"));

        MenuItem hawaiian = createPizza(
                restaurantId,
                "Hawaiian",
                "Ham, pineapple, and extra cheese",
                new BigDecimal("13.99"),
                true,
                false,
                List.of("GLUTEN", "DAIRY"),
                List.of());

        // Drinks
        MenuItem cola = createDrink(
                restaurantId,
                "Coca-Cola",
                "Classic Coca-Cola 500ml",
                new BigDecimal("2.50"),
                true);

        MenuItem water = createDrink(
                restaurantId,
                "Mineral Water",
                "Still mineral water 500ml",
                new BigDecimal("1.50"),
                true);

        // Sides
        MenuItem garlicBread = createSide(
                restaurantId,
                "Garlic Bread",
                "Fresh baked garlic bread with herbs",
                new BigDecimal("4.99"),
                true,
                List.of("GLUTEN", "DAIRY"),
                List.of("VEGETARIAN"));

        MenuItem wings = createSide(
                restaurantId,
                "Chicken Wings",
                "Crispy chicken wings with BBQ sauce (8 pieces)",
                new BigDecimal("8.99"),
                true,
                List.of(),
                List.of());

        // Desserts
        MenuItem tiramisu = createDessert(
                restaurantId,
                "Tiramisu",
                "Classic Italian tiramisu with mascarpone",
                new BigDecimal("6.99"),
                true,
                List.of("GLUTEN", "DAIRY", "EGGS"),
                List.of("VEGETARIAN"));

        // Save all items
        List<MenuItem> items = Arrays.asList(
                margherita, pepperoni, veggiePizza, hawaiian,
                cola, water,
                garlicBread, wings,
                tiramisu);

        menuItemRepository.saveAll(items);
        log.info("Inserted {} sample menu items", items.size());
    }

    private MenuItem createPizza(String restaurantId, String name, String description,
            BigDecimal price, boolean available, boolean featured,
            List<String> allergens, List<String> dietaryTags) {
        return MenuItem.builder()
                .restaurantId(restaurantId)
                .name(name)
                .description(description)
                .category("PIZZA")
                .basePrice(price)
                .imageUrl("/images/pizzas/" + name.toLowerCase().replace(" ", "-") + ".jpg")
                .available(available)
                .featured(featured)
                .preparationTimeMinutes(15)
                .allergens(allergens)
                .dietaryTags(dietaryTags)
                .modifiers(createPizzaModifiers())
                .build();
    }

    private MenuItem createDrink(String restaurantId, String name, String description,
            BigDecimal price, boolean available) {
        return MenuItem.builder()
                .restaurantId(restaurantId)
                .name(name)
                .description(description)
                .category("DRINK")
                .basePrice(price)
                .imageUrl("/images/drinks/" + name.toLowerCase().replace(" ", "-") + ".jpg")
                .available(available)
                .featured(false)
                .preparationTimeMinutes(2)
                .allergens(List.of())
                .dietaryTags(List.of("VEGAN"))
                .build();
    }

    private MenuItem createSide(String restaurantId, String name, String description,
            BigDecimal price, boolean available,
            List<String> allergens, List<String> dietaryTags) {
        return MenuItem.builder()
                .restaurantId(restaurantId)
                .name(name)
                .description(description)
                .category("SIDE")
                .basePrice(price)
                .imageUrl("/images/sides/" + name.toLowerCase().replace(" ", "-") + ".jpg")
                .available(available)
                .featured(false)
                .preparationTimeMinutes(8)
                .allergens(allergens)
                .dietaryTags(dietaryTags)
                .build();
    }

    private MenuItem createDessert(String restaurantId, String name, String description,
            BigDecimal price, boolean available,
            List<String> allergens, List<String> dietaryTags) {
        return MenuItem.builder()
                .restaurantId(restaurantId)
                .name(name)
                .description(description)
                .category("DESSERT")
                .basePrice(price)
                .imageUrl("/images/desserts/" + name.toLowerCase().replace(" ", "-") + ".jpg")
                .available(available)
                .featured(true)
                .preparationTimeMinutes(5)
                .allergens(allergens)
                .dietaryTags(dietaryTags)
                .build();
    }

    private List<MenuItem.Modifier> createPizzaModifiers() {
        return List.of(
                MenuItem.Modifier.builder()
                        .id("size")
                        .name("Size")
                        .type("SIZE")
                        .options(List.of(
                                MenuItem.ModifierOption.builder()
                                        .id("small")
                                        .name("Small (10\")")
                                        .priceAdjustment(new BigDecimal("-2.00"))
                                        .available(true)
                                        .build(),
                                MenuItem.ModifierOption.builder()
                                        .id("medium")
                                        .name("Medium (12\")")
                                        .priceAdjustment(BigDecimal.ZERO)
                                        .available(true)
                                        .build(),
                                MenuItem.ModifierOption.builder()
                                        .id("large")
                                        .name("Large (14\")")
                                        .priceAdjustment(new BigDecimal("3.00"))
                                        .available(true)
                                        .build()))
                        .build(),
                MenuItem.Modifier.builder()
                        .id("toppings")
                        .name("Extra Toppings")
                        .type("TOPPING")
                        .options(List.of(
                                MenuItem.ModifierOption.builder()
                                        .id("extra-cheese")
                                        .name("Extra Cheese")
                                        .priceAdjustment(new BigDecimal("1.50"))
                                        .available(true)
                                        .build(),
                                MenuItem.ModifierOption.builder()
                                        .id("mushrooms")
                                        .name("Mushrooms")
                                        .priceAdjustment(new BigDecimal("1.00"))
                                        .available(true)
                                        .build(),
                                MenuItem.ModifierOption.builder()
                                        .id("olives")
                                        .name("Olives")
                                        .priceAdjustment(new BigDecimal("1.00"))
                                        .available(true)
                                        .build()))
                        .build());
    }
}
