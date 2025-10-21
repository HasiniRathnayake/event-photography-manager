package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "MonthlyFinancialReport")
public class MonthlyFinancialReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReportID")
    private Integer reportID;

    @Column(name = "ReportMonth")
    private Integer reportMonth;

    @Column(name = "ReportYear")
    private Integer reportYear;

    @Column(name = "TotalIncome")
    private BigDecimal totalIncome;

    @Column(name = "TotalExpenses")
    private BigDecimal totalExpenses;

    @Column(name = "NetProfit")
    private BigDecimal netProfit;

    @Column(name = "GeneratedDate")
    private LocalDateTime generatedDate;
}