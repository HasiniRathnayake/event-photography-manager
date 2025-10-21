package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookingID")
    private Integer bookingID;

    @ManyToOne
    @JoinColumn(name = "EventID", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "PhotographerID", nullable = false)
    private Photographer photographer;

    @Column(name = "BookingDate")
    private LocalDateTime bookingDate;

    @Column(name = "Status", columnDefinition = "NVARCHAR(20)")
    private String status;

    @Column(name = "TotalAmount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "AdvancePayment", precision = 10, scale = 2)
    private BigDecimal advancePayment;

    @Column(name = "Notes", columnDefinition = "NVARCHAR(500)")
    private String notes;
}
