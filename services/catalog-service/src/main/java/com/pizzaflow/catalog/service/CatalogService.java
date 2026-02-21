package com.pizzaflow.catalog.service;

import com.pizzaflow.catalog.model.MenuItem;
import com.pizzaflow.catalog.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final MenuItemRepository menuItemRepository;

    @Cacheable(value = "menuItems", key = "#restaurantId")
    public List<MenuItem> getMenuByRestaurant(String restaurantId) {
        log.info("Fetching menu for restaurant: {}", restaurantId);
        return menuItemRepository.findByRestaurantIdAndAvailable(restaurantId, true);
    }

    @Cacheable(value = "menuItem", key = "#id")
    public Optional<MenuItem> getMenuItem(String id) {
        log.info("Fetching menu item: {}", id);
        return menuItemRepository.findById(id);
    }

    @Cacheable(value = "menuItemsByCategory", key = "#restaurantId + '_' + #category")
    public List<MenuItem> getMenuByCategory(String restaurantId, String category) {
        log.info("Fetching menu for restaurant: {} and category: {}", restaurantId, category);
        return menuItemRepository.findByRestaurantIdAndCategory(restaurantId, category);
    }

    @Cacheable(value = "featuredItems", key = "#restaurantId")
    public List<MenuItem> getFeaturedItems(String restaurantId) {
        log.info("Fetching featured items for restaurant: {}", restaurantId);
        return menuItemRepository.findByRestaurantIdAndFeaturedTrue(restaurantId);
    }

    public List<MenuItem> searchMenuItems(String restaurantId, String searchTerm) {
        log.info("Searching menu items for restaurant: {} with term: {}", restaurantId, searchTerm);
        return menuItemRepository.searchByName(restaurantId, searchTerm);
    }

    @CacheEvict(value = { "menuItems", "menuItem", "menuItemsByCategory", "featuredItems" }, allEntries = true)
    public MenuItem createMenuItem(MenuItem menuItem) {
        log.info("Creating new menu item: {}", menuItem.getName());
        return menuItemRepository.save(menuItem);
    }

    @CacheEvict(value = { "menuItems", "menuItem", "menuItemsByCategory", "featuredItems" }, allEntries = true)
    public MenuItem updateMenuItem(String id, MenuItem menuItem) {
        log.info("Updating menu item: {}", id);
        menuItem.setId(id);
        return menuItemRepository.save(menuItem);
    }

    @CacheEvict(value = { "menuItems", "menuItem", "menuItemsByCategory", "featuredItems" }, allEntries = true)
    public void deleteMenuItem(String id) {
        log.info("Deleting menu item: {}", id);
        menuItemRepository.deleteById(id);
    }
}
