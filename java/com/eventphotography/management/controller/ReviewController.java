package com.eventphotography.management.controller;

import com.eventphotography.management.entity.Review;
import com.eventphotography.management.entity.User;
import com.eventphotography.management.entity.Client;
import com.eventphotography.management.entity.Booking;
import com.eventphotography.management.service.ReviewService;
import com.eventphotography.management.service.UserService;
import com.eventphotography.management.service.PhotographerService;
import com.eventphotography.management.service.BookingService;
import com.eventphotography.management.util.UserTypeUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private PhotographerService photographerService;
    
    @Autowired
    private BookingService bookingService;

    @GetMapping
    public String listReviews(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/auth/login";
        }

        if (UserTypeUtils.isEventAttendee(user)) {
            // For clients, we would need to find their client record
            model.addAttribute("reviewList", reviewService.getAllReviews()); // Simplified for now
        } else if (UserTypeUtils.isPhotographer(user)) {
            var photographer = photographerService.getPhotographerByUser(user);
            if (photographer != null) {
                model.addAttribute("reviewList", reviewService.getReviewsByPhotographer(photographer.getPhotographerID()));
            }
        } else {
            model.addAttribute("reviewList", reviewService.getAllReviews());
        }

        model.addAttribute("user", user);
        return "review/list";
    }

    @GetMapping("/create")
    public String showCreateForm(HttpSession session,
                                 @RequestParam("photographerId") Integer photographerId,
                                 Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // Only clients can create reviews
        if (!UserTypeUtils.isEventAttendee(user)) {
            return "redirect:/auth/login";
        }

        model.addAttribute("review", new Review());
        model.addAttribute("photographerId", photographerId);
        model.addAttribute("user", user);
        return "review/create";
    }

    @PostMapping("/create")
    public String createReview(HttpSession session,
                               @ModelAttribute Review review,
                               @RequestParam("photographerId") Integer photographerId,
                               Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // Only clients can create reviews
        if (!UserTypeUtils.isEventAttendee(user)) {
            return "redirect:/auth/login";
        }

        try {
            // Get the client entity for the current user
            Client client = userService.getClientByUserId(user.getUserID());
            
            // Set client
            review.setClient(client);
            
            // Set photographer
            var photographer = photographerService.getPhotographerDetails(photographerId);
            if (photographer == null) {
                model.addAttribute("error", "Invalid photographer selected");
                model.addAttribute("review", review);
                model.addAttribute("photographerId", photographerId);
                return "review/create";
            }
            review.setPhotographer(photographer);
            
            // Set review date and status
            review.setReviewDate(java.time.LocalDateTime.now());
            review.setStatus("Active");
            
            // Find the relevant booking for this client and photographer
            Booking booking = null;
            if (client != null && photographer != null) {
                List<Booking> bookings = bookingService.getBookingsByClientIdAndPhotographerId(client.getClientID(), photographerId);
                if (!bookings.isEmpty()) {
                    // Use the first booking found
                    booking = bookings.get(0);
                }
            }
            
            // Set the booking if found
            if (booking != null) {
                review.setBooking(booking);
            }
            
            Review savedReview = reviewService.createReview(review);
            
            // Update photographer's average rating
            photographerService.updatePhotographerRating(photographerId);
            
            // Notify photographer about new review
            photographerService.notifyNewReview(photographerId, savedReview);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create review: " + e.getMessage());
            model.addAttribute("review", review);
            model.addAttribute("photographerId", photographerId);
            return "review/create";
        }
        
        return "redirect:/reviews";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(HttpSession session,
                               @PathVariable Integer id,
                               Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        Review review = reviewService.getReviewById(id);
        if (review == null) {
            return "redirect:/reviews";
        }

        model.addAttribute("review", review);
        model.addAttribute("user", user);
        return "review/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateReview(HttpSession session,
                               @PathVariable Integer id,
                               @ModelAttribute Review review,
                               Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        Review existingReview = reviewService.getReviewById(id);
        if (existingReview == null) {
            return "redirect:/reviews";
        }

        Integer photographerId = existingReview.getPhotographer().getPhotographerID();
        existingReview.setRating(review.getRating());
        existingReview.setComment(review.getComment());
        existingReview.setStatus(review.getStatus());

        reviewService.updateReview(existingReview);
        
        // Update photographer's average rating
        photographerService.updatePhotographerRating(photographerId);
        return "redirect:/reviews";
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(HttpSession session,
                               @PathVariable Integer id) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        Review review = reviewService.getReviewById(id);
        if (review != null && UserTypeUtils.isAdmin(user)) {
            reviewService.deleteReview(id);
        }

        return "redirect:/reviews";
    }
}