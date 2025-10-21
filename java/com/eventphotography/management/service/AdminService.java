package com.eventphotography.management.service;

import com.eventphotography.management.entity.*;
import com.eventphotography.management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotographerRepository photographerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventRepository eventRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Photographer> getPhotographersForApproval() {
        return photographerRepository.findByIsApprovedFalse();
    }

    public void approvePhotographer(Integer photographerId) {
        Photographer photographer = photographerRepository.findById(photographerId)
                .orElseThrow(() -> new RuntimeException("Photographer not found"));
        photographer.setIsApproved(true);
        photographerRepository.save(photographer);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public void cancelBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("Cancelled");
        bookingRepository.save(booking);
    }

    public void reassignBooking(Integer bookingId, Integer newPhotographerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        Photographer newPhotographer = photographerRepository.findById(newPhotographerId)
                .orElseThrow(() -> new RuntimeException("Photographer not found"));

        booking.setPhotographer(newPhotographer);
        bookingRepository.save(booking);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public void deleteUser(Integer userId) {
        try {
            // First, try to delete the user directly
            userRepository.deleteById(userId);
        } catch (Exception e) {
            // If there's a constraint violation, deactivate the user instead
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setIsActive(false);
            userRepository.save(user);
        }
    }

    public User updateUserRole(Integer userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserType(newRole);
        return userRepository.save(user);
    }
}
