package com.eventphotography.management.service;

import com.eventphotography.management.entity.Client;
import com.eventphotography.management.entity.Event;
import com.eventphotography.management.repository.ClientRepository;
import com.eventphotography.management.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public List<Event> getPublicEvents() {
        return eventRepository.findByStatus("Completed");
    }

    public Event getEventById(Integer eventId) {
        // Use the new method that ensures client and user relationships are loaded
        try {
            return eventRepository.findEventWithClientById(eventId);
        } catch (Exception e) {
            // Fallback to the original method if the new one fails
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            // Ensure the client relationship is loaded
            if (event.getClient() != null) {
                event.getClient().getUser(); // This will trigger loading of the user
            }
            return event;
        }
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public List<Event> getEventsByClient(Integer clientId) {
        return eventRepository.findByClient_ClientID(clientId);
    }

    public List<Event> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents();
    }
    
    public Client getClientByUserId(Integer userId) {
        return clientRepository.findByUserId(userId);
    }
    
    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }
    
    public void markEventAsCompleted(Integer eventId) {
        Event event = getEventById(eventId);
        event.setStatus("Completed");
        saveEvent(event);
    }
}