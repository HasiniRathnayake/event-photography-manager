package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Photographer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface PhotographerRepository extends JpaRepository<Photographer, Integer> {

    @Query("SELECT p FROM Photographer p WHERE p.user.userID = :userId")
    Photographer findByUserId(@Param("userId") Integer userId);

    @Query("SELECT p FROM Photographer p WHERE p.isApproved = false")
    List<Photographer> findByIsApprovedFalse();

    @Query("SELECT p FROM Photographer p WHERE p.user.isActive = true AND " +
            "NOT EXISTS (SELECT b FROM Booking b WHERE b.photographer = p AND " +
            "b.event.date = :date AND " +
            "((b.event.startTime <= :endTime AND b.event.endTime >= :startTime)))")
    List<Photographer> findAvailablePhotographers(@Param("date") LocalDate date,
                                                  @Param("startTime") LocalTime startTime,
                                                  @Param("endTime") LocalTime endTime);
}