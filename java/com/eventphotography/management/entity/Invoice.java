package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InvoiceID")
    private Integer invoiceID;

    @ManyToOne
    @JoinColumn(name = "BookingID", nullable = false)
    private Booking booking;

    @Column(name = "InvoiceDate")
    private LocalDateTime invoiceDate;

    @Column(name = "DueDate")
    private LocalDateTime dueDate;

    @Column(name = "TotalAmount")
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    private BigDecimal totalAmount;

    @Column(name = "AmountPaid")
    @DecimalMin(value = "0.00", message = "Amount paid cannot be negative")
    private BigDecimal amountPaid;

    @Column(name = "Status", columnDefinition = "NVARCHAR(20)")
    private String status;

    @Column(name = "Notes", columnDefinition = "NVARCHAR(500)")
    private String notes;
}