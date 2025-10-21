package com.eventphotography.management.service;

import com.eventphotography.management.entity.Booking;
import com.eventphotography.management.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public void deleteBooking(Integer bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    public List<Booking> getBookingsByPhotographer(Integer photographerId) {
        return bookingRepository.findByPhotographerId(photographerId);
    }

    public List<Booking> getBookingsByEvent(Integer eventId) {
        return bookingRepository.findByEventId(eventId);
    }
    
    public List<Booking> getBookingsByClientIdAndPhotographerId(Integer clientId, Integer photographerId) {
        return bookingRepository.findByClientIdAndPhotographerId(clientId, photographerId);
    }

    public Booking getBookingById(Integer bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
}