package com.eventphotography.management.controller;

import com.eventphotography.management.entity.Payment;
import com.eventphotography.management.entity.User;
import com.eventphotography.management.service.PaymentService;
import com.eventphotography.management.service.FinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private FinancialService financialService;

    @GetMapping
    public String listPayments(@SessionAttribute("user") User user, Model model) {
        // Payments can be viewed by admins and financial managers
        if (!"ADMIN".equals(user.getUserType()) && !"FINANCIAL".equals(user.getUserType())) {
            return "redirect:/auth/login";
        }

        model.addAttribute("paymentList", paymentService.getAllPayments());
        model.addAttribute("user", user);
        return "payment/list";
    }

    @GetMapping("/create")
    public String showCreateForm(@SessionAttribute("user") User user, 
                                 @RequestParam("invoiceId") Integer invoiceId,
                                 Model model) {
        // Payments can be created by admins and financial managers
        if (!"ADMIN".equals(user.getUserType()) && !"FINANCIAL".equals(user.getUserType())) {
            return "redirect:/auth/login";
        }

        model.addAttribute("payment", new Payment());
        model.addAttribute("invoiceId", invoiceId);
        model.addAttribute("user", user);
        return "payment/create";
    }

    @PostMapping("/create")
    public String createPayment(@SessionAttribute("user") User user,
                                @ModelAttribute Payment payment,
                                @RequestParam("invoiceId") Integer invoiceId,
                                Model model) {
        // Payments can be created by admins and financial managers
        if (!"ADMIN".equals(user.getUserType()) && !"FINANCIAL".equals(user.getUserType())) {
            return "redirect:/auth/login";
        }

        // Set the invoice
        var invoice = financialService.getAllInvoices().stream()
                .filter(inv -> inv.getInvoiceID().equals(invoiceId))
                .findFirst()
                .orElse(null);
        
        if (invoice == null) {
            model.addAttribute("error", "Invalid invoice selected");
            model.addAttribute("payment", payment);
            model.addAttribute("invoiceId", invoiceId);
            return "payment/create";
        }

        payment.setInvoice(invoice);
        paymentService.createPayment(payment);
        return "redirect:/financial/invoices";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@SessionAttribute("user") User user,
                               @PathVariable Integer id,
                               Model model) {
        // Payments can be edited by admins and financial managers
        if (!"ADMIN".equals(user.getUserType()) && !"FINANCIAL".equals(user.getUserType())) {
            return "redirect:/auth/login";
        }

        Payment payment = paymentService.getPaymentById(id);
        if (payment == null) {
            return "redirect:/payments";
        }

        model.addAttribute("payment", payment);
        model.addAttribute("user", user);
        return "payment/edit";
    }

    @PostMapping("/edit/{id}")
    public String updatePayment(@SessionAttribute("user") User user,
                                @PathVariable Integer id,
                                @ModelAttribute Payment payment,
                                Model model) {
        // Payments can be edited by admins and financial managers
        if (!"ADMIN".equals(user.getUserType()) && !"FINANCIAL".equals(user.getUserType())) {
            return "redirect:/auth/login";
        }

        Payment existingPayment = paymentService.getPaymentById(id);
        if (existingPayment == null) {
            return "redirect:/payments";
        }

        existingPayment.setAmount(payment.getAmount());
        existingPayment.setPaymentMethod(payment.getPaymentMethod());
        existingPayment.setTransactionID(payment.getTransactionID());
        existingPayment.setStatus(payment.getStatus());
        existingPayment.setNotes(payment.getNotes());

        paymentService.updatePayment(existingPayment);
        return "redirect:/payments";
    }

    @PostMapping("/delete/{id}")
    public String deletePayment(@SessionAttribute("user") User user,
                                @PathVariable Integer id) {
        // Payments can be deleted by admins and financial managers
        if (!"ADMIN".equals(user.getUserType()) && !"FINANCIAL".equals(user.getUserType())) {
            return "redirect:/auth/login";
        }

        paymentService.deletePayment(id);
        return "redirect:/payments";
    }
}