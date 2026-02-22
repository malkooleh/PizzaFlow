package com.pizzaflow.booking.model;

import com.pizzaflow.booking.model.enums.TableType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "table_configurations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_table_number_restaurant", columnNames = { "restaurant_id", "table_number" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "table_number", nullable = false, length = 50)
    private String tableNumber;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "min_capacity", nullable = false)
    @Builder.Default
    private Integer minCapacity = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_type", nullable = false, length = 50)
    @Builder.Default
    private TableType tableType = TableType.INDOOR;

    @Column(name = "location_description")
    private String locationDescription;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if the table can accommodate the given party size.
     */
    public boolean canAccommodate(int partySize) {
        return partySize >= minCapacity && partySize <= capacity;
    }

    /**
     * Get display name - returns name if set, otherwise tableNumber.
     */
    public String getDisplayName() {
        return name != null && !name.isEmpty() ? name : tableNumber;
    }
}
