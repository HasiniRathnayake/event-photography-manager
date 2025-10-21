package com.eventphotography.management.service;

import com.eventphotography.management.entity.Review;
import com.eventphotography.management.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public List<Review> getReviewsByPhotographer(Integer photographerId) {
        return reviewRepository.findByPhotographer_PhotographerID(photographerId);
    }

    public List<Review> getReviewsByClient(Integer clientId) {
        return reviewRepository.findByClient_ClientID(clientId);
    }

    public List<Review> getReviewsByStatus(String status) {
        return reviewRepository.findByStatus(status);
    }

    public Review getReviewById(Integer reviewId) {
        return reviewRepository.findById(reviewId).orElse(null);
    }

    @Transactional
    public Review createReview(Review review) {
        System.out.println("Creating review with:");
        System.out.println("  Client ID: " + (review.getClient() != null ? review.getClient().getClientID() : "null"));
        System.out.println("  Photographer ID: " + (review.getPhotographer() != null ? review.getPhotographer().getPhotographerID() : "null"));
        System.out.println("  Booking ID: " + review.getBookingID());
        System.out.println("  Rating: " + review.getRating());
        System.out.println("  Comment: " + review.getComment());
        Review savedReview = reviewRepository.save(review);
        System.out.println("Review saved with ID: " + (savedReview != null ? savedReview.getReviewID() : "null"));
        return savedReview;
    }

    @Transactional
    public Review updateReview(Review review) {
        return reviewRepository.save(review);
    }

    @Transactional
    public boolean deleteReview(Integer reviewId) {
        if (reviewRepository.existsById(reviewId)) {
            reviewRepository.deleteById(reviewId);
            return true;
        }
        return false;
    }
}