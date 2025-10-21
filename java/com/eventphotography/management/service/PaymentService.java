package com.eventphotography.management.service;

import com.eventphotography.management.entity.Payment;
import com.eventphotography.management.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByInvoice(Integer invoiceId) {
        return paymentRepository.findByInvoice_InvoiceID(invoiceId);
    }

    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    public Payment getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId).orElse(null);
    }

    @Transactional
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment updatePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Transactional
    public boolean deletePayment(Integer paymentId) {
        if (paymentRepository.existsById(paymentId)) {
            paymentRepository.deleteById(paymentId);
            return true;
        }
        return false;
    }
}