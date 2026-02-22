package com.pizzaflow.booking.dto;

import com.pizzaflow.booking.model.enums.TableType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TimeSlotDTO(
    LocalDateTime startTime,
    LocalDateTime endTime,
    int availableCapacity,
    List<AvailableTableDTO> availableTables
) {
    public record AvailableTableDTO(
        UUID tableId,
        String tableName,
        int capacity,
        TableType tableType
    ) {}
}
