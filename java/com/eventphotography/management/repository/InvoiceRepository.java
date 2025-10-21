package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByStatus(String status);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < CURRENT_TIMESTAMP AND (i.status = 'Unpaid' OR i.status = 'Partial')")
    List<Invoice> findOverdueInvoices();

    List<Invoice> findByBooking_BookingID(Integer bookingID);
}
