package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query("SELECT b FROM Booking b WHERE b.photographer.photographerID = :photographerId")
    List<Booking> findByPhotographerId(@Param("photographerId") Integer photographerId);

    @Query("SELECT b FROM Booking b WHERE b.event.eventID = :eventId")
    List<Booking> findByEventId(@Param("eventId") Integer eventId);

    @Query("SELECT b FROM Booking b WHERE b.status = :status")
    List<Booking> findByStatus(@Param("status") String status);
    
    // Add method to find bookings by client and photographer
    @Query("SELECT b FROM Booking b WHERE b.event.client.clientID = :clientId AND b.photographer.photographerID = :photographerId")
    List<Booking> findByClientIdAndPhotographerId(@Param("clientId") Integer clientId, @Param("photographerId") Integer photographerId);
}