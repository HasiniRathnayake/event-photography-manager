package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Expense")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ExpenseID")
    private Integer expenseID;

    @ManyToOne
    @JoinColumn(name = "PhotographerID", nullable = false)
    private Photographer photographer;

    @Column(name = "ExpenseType", columnDefinition = "NVARCHAR(50)")
    private String expenseType;

    @Column(name = "Amount")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Column(name = "ExpenseDate")
    private LocalDateTime expenseDate;

    @Column(name = "Description", columnDefinition = "NVARCHAR(300)")
    private String description;

    @Column(name = "ReceiptImagePath", columnDefinition = "NVARCHAR(500)")
    private String receiptImagePath;

    @Column(name = "Status", columnDefinition = "NVARCHAR(20)")
    private String status;
}
