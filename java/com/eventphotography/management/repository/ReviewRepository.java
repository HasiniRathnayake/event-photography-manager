package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByPhotographer_PhotographerID(Integer photographerID);
    List<Review> findByClient_ClientID(Integer clientID);
    List<Review> findByStatus(String status);
    
    @Query("SELECT r FROM Review r WHERE r.photographer.photographerID = :photographerID ORDER BY r.reviewDate DESC")
    List<Review> findTop5ByPhotographer_PhotographerIDOrderByReviewDateDesc(@Param("photographerID") Integer photographerID);
    
    List<Review> findByPhotographer_PhotographerIDAndStatus(Integer photographerID, String status);
}