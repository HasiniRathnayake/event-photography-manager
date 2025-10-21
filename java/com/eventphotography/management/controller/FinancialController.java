package com.eventphotography.management.controller;

import com.eventphotography.management.entity.*;
import com.eventphotography.management.dto.FinancialReportDTO;
import com.eventphotography.management.service.FinancialService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/financial")
public class FinancialController {

    @Autowired
    private FinancialService financialService;

    // Helper method to check if user has access to financial dashboard
    private boolean hasFinancialAccess(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        String userType = (String) session.getAttribute("userType");
        return currentUser != null && ("Finance Team".equals(userType) || "Photographer".equals(userType));
    }

    // Invoice endpoints
    @GetMapping("/invoices")
    public String listInvoices(Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        model.addAttribute("invoices", financialService.getAllInvoices());
        return "financial/invoices/list";
    }

    @GetMapping("/invoices/unpaid")
    public String listUnpaidInvoices(Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        model.addAttribute("invoices", financialService.getUnpaidInvoices());
        return "financial/invoices/list";
    }

    @GetMapping("/invoices/overdue")
    public String listOverdueInvoices(Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        model.addAttribute("invoices", financialService.getOverdueInvoices());
        return "financial/invoices/list";
    }

    @PostMapping("/invoices/notify-overdue")
    public String notifyOverdueInvoices(Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        financialService.notifyOverdueInvoices();
        model.addAttribute("success", "Overdue invoice notifications sent successfully!");
        model.addAttribute("invoices", financialService.getOverdueInvoices());
        return "financial/invoices/list";
    }

    @GetMapping("/invoices/create")
    public String showCreateInvoiceForm(Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        model.addAttribute("invoice", new Invoice());
        model.addAttribute("bookings", financialService.getAllBookings());
        return "financial/invoices/create";
    }

    @PostMapping("/invoices/create")
    public String createInvoice(@Valid @ModelAttribute Invoice invoice,
                                BindingResult bindingResult,
                                @RequestParam("booking") Integer bookingId,
                                @RequestParam("dueDateInput") String dueDateStr,
                                Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please correct the errors: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("invoice", invoice);
            model.addAttribute("bookings", financialService.getAllBookings());
            return "financial/invoices/create";
        }

        try {
            Booking selectedBooking = financialService.getAllBookings().stream()
                    .filter(booking -> booking.getBookingID().equals(bookingId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

            invoice.setBooking(selectedBooking);

            if (dueDateStr != null && !dueDateStr.isEmpty()) {
                LocalDate dueDate = LocalDate.parse(dueDateStr);
                invoice.setDueDate(dueDate.atStartOfDay());
            }

            if (invoice.getInvoiceDate() == null) {
                invoice.setInvoiceDate(LocalDateTime.now());
            }

            if (invoice.getStatus() == null || invoice.getStatus().isEmpty()) {
                invoice.setStatus("Unpaid");
            }

            if (invoice.getAmountPaid() == null) {
                invoice.setAmountPaid(BigDecimal.ZERO);
            }

            financialService.createInvoice(invoice);
            return "redirect:/financial/invoices";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("invoice", invoice);
            model.addAttribute("bookings", financialService.getAllBookings());
            return "financial/invoices/create";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create invoice: " + e.getMessage());
            model.addAttribute("invoice", invoice);
            model.addAttribute("bookings", financialService.getAllBookings());
            return "financial/invoices/create";
        }
    }

    @PostMapping("/invoices/{id}/payment")
    public String recordPayment(@PathVariable Integer id, @RequestParam BigDecimal amount, Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        try {
            financialService.updateInvoicePayment(id, amount);
            return "redirect:/financial/invoices";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/financial/invoices";
        }
    }

    @PostMapping("/invoices/{id}/delete")
    public String deleteInvoice(@PathVariable Integer id, Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        boolean deleted = financialService.deleteInvoice(id);
        if (!deleted) {
            model.addAttribute("error", "Failed to delete invoice. It may be referenced by other records.");
        }
        return "redirect:/financial/invoices";
    }

    // Expense endpoints
    @GetMapping("/expenses")
    public String listExpenses(Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        model.addAttribute("expenses", financialService.getAllExpenses());
        return "financial/expenses/list";
    }

    @GetMapping("/expenses/create")
    public String showCreateExpenseForm(Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        model.addAttribute("expense", new Expense());
        return "financial/expenses/create";
    }

    @PostMapping("/expenses/create")
    public String createExpense(@Valid @ModelAttribute Expense expense,
                                BindingResult bindingResult,
                                @RequestParam("expenseDateInput") String expenseDateStr,
                                Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please correct the errors: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("expense", expense);
            return "financial/expenses/create";
        }

        try {
            if (expenseDateStr != null && !expenseDateStr.isEmpty()) {
                LocalDate expenseDate = LocalDate.parse(expenseDateStr);
                expense.setExpenseDate(expenseDate.atStartOfDay());
            }

            financialService.createExpense(expense);
            return "redirect:/financial/expenses";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("expense", expense);
            return "financial/expenses/create";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create expense: " + e.getMessage());
            model.addAttribute("expense", expense);
            return "financial/expenses/create";
        }
    }

    @PostMapping("/expenses/{id}/delete")
    public String deleteExpense(@PathVariable Integer id, Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        boolean deleted = financialService.deleteExpense(id);
        if (!deleted) {
            model.addAttribute("error", "Failed to delete expense. It may be referenced by other records.");
        }
        return "redirect:/financial/expenses";
    }

    // Report endpoints
    @GetMapping("/reports")
    public String showReports(Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        return "financial/reports/generate";
    }

    @GetMapping("/reports/list")
    public String listReports(Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        model.addAttribute("reports", financialService.getAllMonthlyReports());
        return "financial/reports/list";
    }

    @PostMapping("/reports/generate")
    public String generateReport(@RequestParam Integer month, @RequestParam Integer year, Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        MonthlyFinancialReport report = financialService.generateMonthlyReport(month, year);
        FinancialReportDTO reportDTO = new FinancialReportDTO(report);
        model.addAttribute("reportDTO", reportDTO);
        return "financial/reports/view";
    }

    @GetMapping("/reports/view")
    public String viewReport(@RequestParam Integer month, @RequestParam Integer year, Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        Optional<MonthlyFinancialReport> report = financialService.getMonthlyReport(month, year);
        if (report.isPresent()) {
            FinancialReportDTO reportDTO = new FinancialReportDTO(report.get());
            model.addAttribute("reportDTO", reportDTO);
            return "financial/reports/view";
        } else {
            model.addAttribute("error", "Report not found for the specified month and year");
            return "financial/reports/generate";
        }
    }

    @PostMapping("/reports/{id}/delete")
    public String deleteReport(@PathVariable Integer id, Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        boolean deleted = financialService.deleteMonthlyReport(id);
        if (!deleted) {
            model.addAttribute("error", "Failed to delete report.");
        }
        return "redirect:/financial/reports/list";
    }

    // Dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        if (!hasFinancialAccess(session)) {
            return "redirect:/auth/login";
        }
        model.addAttribute("totalInvoices", financialService.getAllInvoices().size());
        model.addAttribute("unpaidInvoices", financialService.getUnpaidInvoices().size());
        model.addAttribute("overdueInvoices", financialService.getOverdueInvoices().size());
        model.addAttribute("totalExpenses", financialService.getAllExpenses().size());

        BigDecimal totalRevenue = financialService.getAllInvoices().stream()
                .map(inv -> inv.getAmountPaid() != null ? inv.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingRevenue = financialService.getUnpaidInvoices().stream()
                .map(inv -> inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenseAmount = financialService.getAllExpenses().stream()
                .filter(exp -> "Approved".equals(exp.getStatus()))
                .map(exp -> exp.getAmount() != null ? exp.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("pendingRevenue", pendingRevenue);
        model.addAttribute("totalExpenseAmount", totalExpenseAmount);
        model.addAttribute("netProfit", totalRevenue.subtract(totalExpenseAmount));

        model.addAttribute("recentInvoices", financialService.getAllInvoices().stream()
                .sorted((a, b) -> b.getInvoiceDate().compareTo(a.getInvoiceDate()))
                .limit(5)
                .toList());

        model.addAttribute("recentExpenses", financialService.getAllExpenses().stream()
                .sorted((a, b) -> b.getExpenseDate().compareTo(a.getExpenseDate()))
                .limit(5)
                .toList());

        return "financial/dashboard";
    }
}