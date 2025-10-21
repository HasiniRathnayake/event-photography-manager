package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Package")
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PackageID")
    private Integer packageID;

    @ManyToOne
    @JoinColumn(name = "PhotographerID", nullable = false)
    private Photographer photographer;

    @Column(name = "Name", columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(name = "Description", columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(name = "DurationHours")
    private Integer durationHours;

    @Column(name = "Price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "NumberOfPhotos")
    private Integer numberOfPhotos;

    @Column(name = "IsEditingIncluded")
    private Boolean isEditingIncluded = false;

    @Column(name = "EditingTimeDays")
    private Integer editingTimeDays;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "Status", columnDefinition = "NVARCHAR(20)")
    private String status; // Active, Inactive

    @Column(name = "PackageType", columnDefinition = "NVARCHAR(50)")
    private String packageType; // Basic, Standard, Premium, Custom
}