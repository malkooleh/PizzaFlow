package com.pizzaflow.inventory.exception;

public class InventoryItemNotFoundException extends RuntimeException {

    public InventoryItemNotFoundException(String itemId) {
        super("Inventory item not found: " + itemId);
    }
}
