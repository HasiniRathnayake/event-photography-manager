package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "Photographer")
public class Photographer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PhotographerID")
    private Integer photographerID;

    @OneToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "Specialization", columnDefinition = "NVARCHAR(100)")
    private String specialization;

    @Column(name = "Experience")
    private Integer experience;

    @Column(name = "Portfolio", columnDefinition = "NVARCHAR(500)")
    private String portfolio;

    @Column(name = "Equipment", columnDefinition = "NVARCHAR(300)")
    private String equipment;

    @Column(name = "Rating", precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "PricePerHour", precision = 8, scale = 2)
    private BigDecimal pricePerHour;

    @Column(name = "Availability", columnDefinition = "NVARCHAR(200)")
    private String availability;

    @Column(name = "ProfilePhotoPath", columnDefinition = "NVARCHAR(200)")
    private String profilePhotoPath;

    @Column(name = "IsApproved")
    private Boolean isApproved = false;
}