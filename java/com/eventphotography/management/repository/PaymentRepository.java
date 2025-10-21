package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByInvoice_InvoiceID(Integer invoiceID);
    List<Payment> findByStatus(String status);
}