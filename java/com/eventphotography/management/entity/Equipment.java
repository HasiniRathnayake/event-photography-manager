package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EquipmentID")
    private Integer equipmentID;

    @ManyToOne
    @JoinColumn(name = "PhotographerID", nullable = false)
    private Photographer photographer;

    @Column(name = "Name", columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(name = "Description", columnDefinition = "NVARCHAR(300)")
    private String description;

    @Column(name = "Category", columnDefinition = "NVARCHAR(50)")
    private String category;

    @Column(name = "PurchaseDate")
    private LocalDateTime purchaseDate;

    @Column(name = "Value", precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "Status", columnDefinition = "NVARCHAR(20)")
    private String status; // Available, In Use, Maintenance, Retired

    @Column(name = "Condition", columnDefinition = "NVARCHAR(50)")
    private String condition; // New, Good, Fair, Poor

    @Column(name = "SerialNumber", columnDefinition = "NVARCHAR(100)")
    private String serialNumber;
}