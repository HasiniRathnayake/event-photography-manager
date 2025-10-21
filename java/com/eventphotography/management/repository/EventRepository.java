package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    List<Event> findByClient_ClientID(Integer clientId);
    List<Event> findByStatus(String status);

    @Query("SELECT e FROM Event e WHERE e.status = 'Completed'")
    List<Event> findCompletedEvents();

    @Query("SELECT e FROM Event e WHERE e.status = 'Confirmed' AND e.date >= CURRENT_DATE")
    List<Event> findUpcomingEvents();
    
    @Query("SELECT e FROM Event e WHERE e.status = 'Confirmed' AND e.date >= CURRENT_DATE AND EXISTS (SELECT 1 FROM Booking b WHERE b.event.eventID = e.eventID AND b.photographer.photographerID = :photographerId)")
    List<Event> findUpcomingEventsByPhotographer(@Param("photographerId") Integer photographerId);
    
    @Query("SELECT e FROM Event e WHERE e.date < CURRENT_DATE AND EXISTS (SELECT 1 FROM Booking b WHERE b.event.eventID = e.eventID AND b.photographer.photographerID = :photographerId)")
    List<Event> findPastEventsForAlbumCreation(@Param("photographerId") Integer photographerId);
    
    // New method to find all events (past and current) for album creation
    @Query("SELECT e FROM Event e WHERE EXISTS (SELECT 1 FROM Booking b WHERE b.event.eventID = e.eventID AND b.photographer.photographerID = :photographerId)")
    List<Event> findAllEventsForPhotographer(@Param("photographerId") Integer photographerId);
    
    @Query("SELECT e FROM Event e WHERE e.client.clientID = :clientId")
    List<Event> findEventsByClientId(@Param("clientId") Integer clientId);
    
    // Fetch event with client and user relationship loaded
    @Query("SELECT e FROM Event e JOIN FETCH e.client c JOIN FETCH c.user WHERE e.eventID = :eventId")
    Event findEventWithClientById(@Param("eventId") Integer eventId);
}