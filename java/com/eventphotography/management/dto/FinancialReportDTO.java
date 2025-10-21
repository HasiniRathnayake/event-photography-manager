package com.eventphotography.management.dto;

import com.eventphotography.management.entity.MonthlyFinancialReport;
import java.math.BigDecimal;

public class FinancialReportDTO {
    private MonthlyFinancialReport report;
    private BigDecimal eventPhotographyPercentage;
    private BigDecimal packageSalesPercentage;
    private BigDecimal otherIncomePercentage;
    private BigDecimal equipmentPercentage;
    private BigDecimal travelPercentage;
    private BigDecimal marketingPercentage;
    private BigDecimal otherExpensesPercentage;
    private BigDecimal incomeChartPercentage;
    private BigDecimal expenseChartPercentage;
    
    public FinancialReportDTO(MonthlyFinancialReport report) {
        this.report = report;
        calculatePercentages();
    }
    
    private void calculatePercentages() {
        BigDecimal totalIncome = report.getTotalIncome() != null ? report.getTotalIncome() : BigDecimal.ZERO;
        BigDecimal totalExpenses = report.getTotalExpenses() != null ? report.getTotalExpenses() : BigDecimal.ZERO;
        
        // Calculate income breakdown percentages
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            eventPhotographyPercentage = totalIncome.multiply(BigDecimal.valueOf(0.85));
            packageSalesPercentage = totalIncome.multiply(BigDecimal.valueOf(0.10));
            otherIncomePercentage = totalIncome.multiply(BigDecimal.valueOf(0.05));
        } else {
            eventPhotographyPercentage = BigDecimal.ZERO;
            packageSalesPercentage = BigDecimal.ZERO;
            otherIncomePercentage = BigDecimal.ZERO;
        }
        
        // Calculate expense breakdown percentages
        if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
            equipmentPercentage = totalExpenses.multiply(BigDecimal.valueOf(0.40));
            travelPercentage = totalExpenses.multiply(BigDecimal.valueOf(0.25));
            marketingPercentage = totalExpenses.multiply(BigDecimal.valueOf(0.20));
            otherExpensesPercentage = totalExpenses.multiply(BigDecimal.valueOf(0.15));
        } else {
            equipmentPercentage = BigDecimal.ZERO;
            travelPercentage = BigDecimal.ZERO;
            marketingPercentage = BigDecimal.ZERO;
            otherExpensesPercentage = BigDecimal.ZERO;
        }
        
        // Calculate chart percentages
        incomeChartPercentage = BigDecimal.valueOf(50); // Default 50%
        expenseChartPercentage = BigDecimal.valueOf(50); // Default 50%
        
        BigDecimal totalIncomeAndExpenses = totalIncome.add(totalExpenses);
        if (totalIncomeAndExpenses.compareTo(BigDecimal.ZERO) > 0) {
            incomeChartPercentage = totalIncome.divide(totalIncomeAndExpenses, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            expenseChartPercentage = totalExpenses.divide(totalIncomeAndExpenses, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }
    
    // Getters and setters
    public MonthlyFinancialReport getReport() {
        return report;
    }
    
    public void setReport(MonthlyFinancialReport report) {
        this.report = report;
    }
    
    public BigDecimal getEventPhotographyPercentage() {
        return eventPhotographyPercentage;
    }
    
    public void setEventPhotographyPercentage(BigDecimal eventPhotographyPercentage) {
        this.eventPhotographyPercentage = eventPhotographyPercentage;
    }
    
    public BigDecimal getPackageSalesPercentage() {
        return packageSalesPercentage;
    }
    
    public void setPackageSalesPercentage(BigDecimal packageSalesPercentage) {
        this.packageSalesPercentage = packageSalesPercentage;
    }
    
    public BigDecimal getOtherIncomePercentage() {
        return otherIncomePercentage;
    }
    
    public void setOtherIncomePercentage(BigDecimal otherIncomePercentage) {
        this.otherIncomePercentage = otherIncomePercentage;
    }
    
    public BigDecimal getEquipmentPercentage() {
        return equipmentPercentage;
    }
    
    public void setEquipmentPercentage(BigDecimal equipmentPercentage) {
        this.equipmentPercentage = equipmentPercentage;
    }
    
    public BigDecimal getTravelPercentage() {
        return travelPercentage;
    }
    
    public void setTravelPercentage(BigDecimal travelPercentage) {
        this.travelPercentage = travelPercentage;
    }
    
    public BigDecimal getMarketingPercentage() {
        return marketingPercentage;
    }
    
    public void setMarketingPercentage(BigDecimal marketingPercentage) {
        this.marketingPercentage = marketingPercentage;
    }
    
    public BigDecimal getOtherExpensesPercentage() {
        return otherExpensesPercentage;
    }
    
    public void setOtherExpensesPercentage(BigDecimal otherExpensesPercentage) {
        this.otherExpensesPercentage = otherExpensesPercentage;
    }
    
    public BigDecimal getIncomeChartPercentage() {
        return incomeChartPercentage;
    }
    
    public void setIncomeChartPercentage(BigDecimal incomeChartPercentage) {
        this.incomeChartPercentage = incomeChartPercentage;
    }
    
    public BigDecimal getExpenseChartPercentage() {
        return expenseChartPercentage;
    }
    
    public void setExpenseChartPercentage(BigDecimal expenseChartPercentage) {
        this.expenseChartPercentage = expenseChartPercentage;
    }
}