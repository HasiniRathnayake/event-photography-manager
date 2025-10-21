package com.eventphotography.management.service;

import com.eventphotography.management.entity.*;
import com.eventphotography.management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Service
public class FinancialService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MonthlyFinancialReportRepository reportRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // Invoice methods
    public List<Invoice> getAllInvoices() {
        updateOverdueInvoices();
        return invoiceRepository.findAll();
    }

    public List<Invoice> getUnpaidInvoices() {
        updateOverdueInvoices();
        return invoiceRepository.findByStatus("Unpaid");
    }

    public List<Invoice> getOverdueInvoices() {
        updateOverdueInvoices();
        return invoiceRepository.findByStatus("Overdue");
    }

    public void notifyOverdueInvoices() {
        List<Invoice> overdueInvoices = getOverdueInvoices();
        // In a real implementation, we would send notifications to the finance team
        // For now, we'll just log the overdue invoices
        System.out.println("Overdue invoices notification:");
        for (Invoice invoice : overdueInvoices) {
            System.out.println("Invoice ID: " + invoice.getInvoiceID() + ", Due Date: " + invoice.getDueDate() + ", Amount: " + invoice.getTotalAmount());
        }
    }

    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        if (invoice.getTotalAmount() == null || invoice.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount: Total amount must be greater than zero");
        }

        if (invoice.getAmountPaid() != null && invoice.getAmountPaid().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid amount: Amount paid cannot be negative");
        }

        List<Invoice> existingInvoices = invoiceRepository.findByBooking_BookingID(invoice.getBooking().getBookingID());
        if (!existingInvoices.isEmpty()) {
            throw new IllegalArgumentException("Only one invoice per booking is allowed");
        }

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice updateInvoicePayment(Integer invoiceId, BigDecimal newPaymentAmount) {
        if (newPaymentAmount == null || newPaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid amount: Payment amount cannot be negative");
        }

        Optional<Invoice> optionalInvoice = invoiceRepository.findById(invoiceId);

        if (optionalInvoice.isPresent()) {
            Invoice invoice = optionalInvoice.get();

            BigDecimal currentAmountPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal totalAmountPaid = currentAmountPaid.add(newPaymentAmount);

            invoice.setAmountPaid(totalAmountPaid);

            if (totalAmountPaid.compareTo(invoice.getTotalAmount()) >= 0) {
                invoice.setStatus("Paid");
            } else if (totalAmountPaid.compareTo(BigDecimal.ZERO) > 0) {
                invoice.setStatus("Partial");
            } else {
                if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(LocalDateTime.now())) {
                    invoice.setStatus("Overdue");
                } else {
                    invoice.setStatus("Unpaid");
                }
            }

            return invoiceRepository.save(invoice);
        }
        return null;
    }

    // Expense methods
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    @Transactional
    public Expense createExpense(Expense expense) {
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount: Expense amount must be greater than zero");
        }

        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByPhotographer(Integer photographerId) {
        return expenseRepository.findByPhotographer_PhotographerID(photographerId);
    }

    // Monthly Report methods
    @Transactional
    public MonthlyFinancialReport generateMonthlyReport(Integer month, Integer year) {
        Optional<MonthlyFinancialReport> existingReport = reportRepository.findByReportMonthAndReportYear(month, year);

        BigDecimal totalIncome = invoiceRepository.findAll().stream()
                .filter(inv -> inv.getStatus() != null &&
                        ("Paid".equals(inv.getStatus()) || "Partial".equals(inv.getStatus())))
                .filter(inv -> inv.getInvoiceDate() != null &&
                        inv.getInvoiceDate().getMonthValue() == month &&
                        inv.getInvoiceDate().getYear() == year)
                .map(inv -> {
                    if ("Paid".equals(inv.getStatus())) {
                        return inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO;
                    } else {
                        return inv.getAmountPaid() != null ? inv.getAmountPaid() : BigDecimal.ZERO;
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = expenseRepository.findAll().stream()
                .filter(exp -> exp.getStatus() != null && "Approved".equals(exp.getStatus()))
                .filter(exp -> exp.getExpenseDate() != null &&
                        exp.getExpenseDate().getMonthValue() == month &&
                        exp.getExpenseDate().getYear() == year)
                .map(exp -> exp.getAmount() != null ? exp.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalIncome.subtract(totalExpenses);
        
        // Calculate income breakdown percentages
        BigDecimal eventPhotographyPercentage = BigDecimal.ZERO;
        BigDecimal packageSalesPercentage = BigDecimal.ZERO;
        BigDecimal otherIncomePercentage = BigDecimal.ZERO;
        
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            eventPhotographyPercentage = totalIncome.multiply(BigDecimal.valueOf(0.85));
            packageSalesPercentage = totalIncome.multiply(BigDecimal.valueOf(0.10));
            otherIncomePercentage = totalIncome.multiply(BigDecimal.valueOf(0.05));
        }
        
        // Calculate expense breakdown percentages
        BigDecimal equipmentPercentage = BigDecimal.ZERO;
        BigDecimal travelPercentage = BigDecimal.ZERO;
        BigDecimal marketingPercentage = BigDecimal.ZERO;
        BigDecimal otherExpensesPercentage = BigDecimal.ZERO;
        
        if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
            equipmentPercentage = totalExpenses.multiply(BigDecimal.valueOf(0.40));
            travelPercentage = totalExpenses.multiply(BigDecimal.valueOf(0.25));
            marketingPercentage = totalExpenses.multiply(BigDecimal.valueOf(0.20));
            otherExpensesPercentage = totalExpenses.multiply(BigDecimal.valueOf(0.15));
        }
        
        // Calculate chart percentages
        BigDecimal incomeChartPercentage = BigDecimal.valueOf(50); // Default 50%
        BigDecimal expenseChartPercentage = BigDecimal.valueOf(50); // Default 50%
        
        BigDecimal totalIncomeAndExpenses = totalIncome.add(totalExpenses);
        if (totalIncomeAndExpenses.compareTo(BigDecimal.ZERO) > 0) {
            incomeChartPercentage = totalIncome.divide(totalIncomeAndExpenses, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            expenseChartPercentage = totalExpenses.divide(totalIncomeAndExpenses, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        MonthlyFinancialReport report;
        if (existingReport.isPresent()) {
            report = existingReport.get();
            report.setTotalIncome(totalIncome);
            report.setTotalExpenses(totalExpenses);
            report.setNetProfit(netProfit);
            report.setGeneratedDate(LocalDateTime.now());
        } else {
            report = new MonthlyFinancialReport();
            report.setReportMonth(month);
            report.setReportYear(year);
            report.setTotalIncome(totalIncome);
            report.setTotalExpenses(totalExpenses);
            report.setNetProfit(netProfit);
            report.setGeneratedDate(LocalDateTime.now());
        }
        
        // Set the calculated values as additional properties
        // We'll need to create a wrapper or extend the entity to include these values
        // For now, we'll pass them separately in the controller

        return reportRepository.save(report);
    }

    public Optional<MonthlyFinancialReport> getMonthlyReport(Integer month, Integer year) {
        return reportRepository.findByReportMonthAndReportYear(month, year);
    }

    // Delete methods
    @Transactional
    public boolean deleteInvoice(Integer invoiceId) {
        try {
            if (invoiceRepository.existsById(invoiceId)) {
                invoiceRepository.deleteById(invoiceId);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public boolean deleteExpense(Integer expenseId) {
        try {
            if (expenseRepository.existsById(expenseId)) {
                expenseRepository.deleteById(expenseId);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public boolean deleteMonthlyReport(Integer reportId) {
        try {
            if (reportRepository.existsById(reportId)) {
                reportRepository.deleteById(reportId);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public List<MonthlyFinancialReport> getAllMonthlyReports() {
        return reportRepository.findAll();
    }

    // Booking methods
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Private method to update overdue invoices
    @Transactional
    protected void updateOverdueInvoices() {
        List<Invoice> unpaidInvoices = invoiceRepository.findByStatus("Unpaid");
        List<Invoice> partialInvoices = invoiceRepository.findByStatus("Partial");
        LocalDateTime now = LocalDateTime.now();

        for (Invoice invoice : unpaidInvoices) {
            if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(now)) {
                invoice.setStatus("Overdue");
                invoiceRepository.save(invoice);
            }
        }

        for (Invoice invoice : partialInvoices) {
            if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(now)) {
                invoice.setStatus("Overdue");
                invoiceRepository.save(invoice);
            }
        }
    }
}
