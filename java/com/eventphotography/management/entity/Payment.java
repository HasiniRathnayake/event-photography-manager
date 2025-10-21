package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentID")
    private Integer paymentID;

    @ManyToOne
    @JoinColumn(name = "InvoiceID", nullable = false)
    private Invoice invoice;

    @Column(name = "Amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;

    @Column(name = "PaymentMethod", columnDefinition = "NVARCHAR(50)")
    private String paymentMethod; // Credit Card, Debit Card, Bank Transfer, Cash, PayPal

    @Column(name = "TransactionID", columnDefinition = "NVARCHAR(100)")
    private String transactionID;

    @Column(name = "Status", columnDefinition = "NVARCHAR(20)")
    private String status; // Pending, Completed, Failed, Refunded

    @Column(name = "Notes", columnDefinition = "NVARCHAR(500)")
    private String notes;
}