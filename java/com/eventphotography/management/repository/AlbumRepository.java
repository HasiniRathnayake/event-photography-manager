package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Integer> {

    @Query("SELECT a FROM Album a WHERE EXISTS (SELECT 1 FROM Booking b WHERE b.event.eventID = a.event.eventID AND b.photographer.photographerID = :photographerId)")
    List<Album> findByPhotographerId(@Param("photographerId") Integer photographerId);

    List<Album> findByEvent_EventID(Integer eventId);
    
    @Query("SELECT a FROM Album a WHERE a.event.client.clientID = :clientId")
    List<Album> findByClientId(@Param("clientId") Integer clientId);
}