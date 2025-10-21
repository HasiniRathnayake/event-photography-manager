package com.eventphotography.management.controller;

import com.eventphotography.management.entity.Booking;
import com.eventphotography.management.entity.Client;
import com.eventphotography.management.entity.Event;
import com.eventphotography.management.entity.Photographer;
import com.eventphotography.management.entity.User;
import com.eventphotography.management.repository.EventRepository;
import com.eventphotography.management.service.BookingService;
import com.eventphotography.management.service.EventService;
import com.eventphotography.management.service.PhotographerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/event-organizer")
public class EventOrganizerController {

    @Autowired
    private EventService eventService;

    @Autowired
    private PhotographerService photographerService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Event Organizer".equals(session.getAttribute("userType"))) {
            return "redirect:/auth/login";
        }

        // Get all events for the organizer dashboard
        List<Event> organizerEvents = eventService.getAllEvents();
        
        // Calculate active bookings count (not cancelled nor completed)
        long activeBookingsCount = 0;
        long pendingActionsCount = 0;
        
        // Get all bookings to calculate active bookings
        List<Booking> allBookings = bookingService.getAllBookings();
        for (Booking booking : allBookings) {
            // Count active bookings (not cancelled nor completed)
            if (!"Cancelled".equals(booking.getStatus()) && !"Completed".equals(booking.getStatus())) {
                activeBookingsCount++;
            }
            
            // Count pending actions (pending status)
            if ("Pending".equals(booking.getStatus())) {
                pendingActionsCount++;
            }
        }

        model.addAttribute("events", organizerEvents);
        model.addAttribute("newEvent", new Event());
        model.addAttribute("activeBookingsCount", activeBookingsCount);
        model.addAttribute("pendingActionsCount", pendingActionsCount);

        return "event-organizer/dashboard";
    }

    @PostMapping("/events/create")
    public String createEvent(@RequestParam String eventName,
                              @RequestParam(required = false) String eventType,
                              @RequestParam String date,
                              @RequestParam String startTime,
                              @RequestParam String endTime,
                              @RequestParam String venue,
                              @RequestParam(required = false) String expectedGuests,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String specialRequirements,
                              @RequestParam(required = false) String budget,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null || session.getAttribute("userType") == null ||
                !"Event Organizer".equals(session.getAttribute("userType"))) {
            redirectAttributes.addFlashAttribute("error", "Please log in as an Event Organizer to create events.");
            return "redirect:/auth/login";
        }

        try {
            // Check if client record already exists for this user
            Client client = eventService.getClientByUserId(currentUser.getUserID());
            if (client == null) {
                // Create a new client record for this user
                client = new Client();
                client.setUser(currentUser);
                // Save the client first
                client = eventService.saveClient(client);
            }
            
            // Create and populate the event
            Event event = new Event();
            event.setEventName(eventName);
            // Guard against missing eventType from quick form
            if (eventType == null || eventType.isBlank()) {
                event.setEventType("Other");
            } else {
                event.setEventType(eventType);
            }
            event.setDate(java.time.LocalDate.parse(date));
            event.setStartTime(java.time.LocalTime.parse(startTime));
            event.setEndTime(java.time.LocalTime.parse(endTime));
            event.setVenue(venue);
            if (expectedGuests != null && !expectedGuests.isEmpty()) {
                event.setExpectedGuests(Integer.parseInt(expectedGuests));
            }
            // Append optional budget into description to avoid schema changes
            if (budget != null && !budget.isBlank()) {
                String desc = (description != null ? description : "");
                String budgetLine = (desc.isBlank() ? "" : "\n") + "Estimated Budget: Rs. " + budget;
                event.setDescription(desc + budgetLine);
            } else {
                event.setDescription(description);
            }
            event.setSpecialRequirements(specialRequirements);
            event.setClient(client);
            event.setStatus("Confirmed");
            event.setCreatedDate(java.time.LocalDateTime.now());
            
            eventService.saveEvent(event);
            redirectAttributes.addFlashAttribute("success", "Event created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create event: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/event-organizer/dashboard";
    }

    @GetMapping("/events")
    public String viewEvents(HttpSession session, Model model, 
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) String sort) {
        User currentUser = (User) session.getAttribute("currentUser");
        List<Event> events;
        
        if (search != null && !search.isEmpty()) {
            // Implement search functionality
            events = eventService.getAllEvents().stream()
                .filter(event -> event.getEventName().toLowerCase().contains(search.toLowerCase()) ||
                        event.getDescription().toLowerCase().contains(search.toLowerCase()) ||
                        event.getVenue().toLowerCase().contains(search.toLowerCase()))
                .toList();
        } else {
            // Simplified event retrieval
            events = eventService.getAllEvents();
        }
        
        // Sort events by date if requested
        if ("date".equals(sort)) {
            events = events.stream()
                .sorted((e1, e2) -> e1.getDate().compareTo(e2.getDate()))
                .toList();
        }
        
        model.addAttribute("events", events);
        model.addAttribute("searchQuery", search);
        model.addAttribute("sort", sort);
        return "event-organizer/events";
    }

    @GetMapping("/events/new")
    public String showCreateEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "event-organizer/create-event";
    }

    @GetMapping("/events/{eventId}")
    public String viewEvent(@PathVariable Integer eventId, Model model) {
        Event event = eventService.getEventById(eventId);
        // Get the booking for this event to show the assigned photographer
        List<Booking> bookings = bookingService.getBookingsByEvent(eventId);
        Booking eventBooking = bookings.isEmpty() ? null : bookings.get(0);
        
        model.addAttribute("event", event);
        model.addAttribute("eventBooking", eventBooking);
        return "event-organizer/view-event";
    }

    @GetMapping("/events/{eventId}/edit")
    public String showEditEventForm(@PathVariable Integer eventId, Model model) {
        Event event = eventService.getEventById(eventId);
        model.addAttribute("event", event);
        return "event-organizer/edit-event";
    }

    @PostMapping("/events/{eventId}/update")
    public String updateEvent(@PathVariable Integer eventId,
                              @ModelAttribute Event event) {
        Event existingEvent = eventService.getEventById(eventId);
        existingEvent.setEventName(event.getEventName());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setVenue(event.getVenue());
        existingEvent.setSpecialRequirements(event.getSpecialRequirements());
        eventService.saveEvent(existingEvent);
        return "redirect:/event-organizer/events";
    }

    @GetMapping("/events/{eventId}/assign-photographer")
    public String showAssignPhotographer(@PathVariable Integer eventId,
                                         Model model) {
        // Show all photographers
        List<Photographer> photographers = photographerService.getAllPhotographers();
        
        model.addAttribute("eventId", eventId);
        model.addAttribute("photographers", photographers);
        return "event-organizer/assign-photographer";
    }

    @PostMapping("/events/{eventId}/assign-photographer")
    public String assignPhotographer(@PathVariable Integer eventId,
                                     @RequestParam Integer photographerId,
                                     @RequestParam String notes,
                                     Model model) {
        try {
            System.out.println("Assigning photographer ID: " + photographerId + " to event ID: " + eventId);
            // Check if a booking already exists for this event
            List<Booking> existingBookings = bookingService.getBookingsByEvent(eventId);
            Booking booking;
            
            if (existingBookings.isEmpty()) {
                // Create a new booking
                booking = new Booking();
                Event event = eventService.getEventById(eventId);
                booking.setEvent(event);
                
                Photographer photographer = new Photographer();
                photographer.setPhotographerID(photographerId);
                booking.setPhotographer(photographer);
                
                booking.setBookingDate(java.time.LocalDateTime.now());
                booking.setStatus("Confirmed");
                
                // Extract budget from event description if available
                BigDecimal budgetAmount = java.math.BigDecimal.ZERO;
                if (event.getDescription() != null && event.getDescription().contains("Estimated Budget: Rs.")) {
                    try {
                        String budgetStr = event.getDescription().substring(event.getDescription().indexOf("Estimated Budget: Rs.") + 21);
                        budgetStr = budgetStr.split("\n")[0].trim();
                        budgetAmount = new java.math.BigDecimal(budgetStr);
                    } catch (Exception e) {
                        // If parsing fails, use zero
                    }
                }
                booking.setTotalAmount(budgetAmount);
                booking.setAdvancePayment(java.math.BigDecimal.ZERO);
                booking.setNotes(notes);
                
                // Save the booking
                Booking savedBooking = bookingService.saveBooking(booking);
                booking = savedBooking; // Update the booking reference
            } else {
                // Update existing booking
                booking = existingBookings.get(0);
                Photographer photographer = new Photographer();
                photographer.setPhotographerID(photographerId);
                booking.setPhotographer(photographer);
                booking.setNotes(notes);
                booking.setStatus("Confirmed");
                
                // Save the booking
                Booking savedBooking = bookingService.saveBooking(booking);
                booking = savedBooking; // Update the booking reference
            }
            
            // Update event status to Confirmed when photographer is assigned
            Event event = eventService.getEventById(eventId);
            event.setStatus("Confirmed");
            eventService.saveEvent(event);
            
            // Notify the photographer
            System.out.println("Notifying photographer about booking ID: " + booking.getBookingID());
            photographerService.notifyNewBooking(photographerId, booking.getBookingID());
            
            model.addAttribute("success", "Photographer assigned successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to assign photographer: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/event-organizer/events";
    }

    @PostMapping("/events/{eventId}/send-reminder")
    public String sendEventReminder(@PathVariable Integer eventId) {
        // Implementation for sending event reminder
        return "redirect:/event-organizer/events";
    }

    @GetMapping("/events/{eventId}/cancel")
    public String cancelEvent(@PathVariable Integer eventId) {
        Event event = eventService.getEventById(eventId);
        event.setStatus("Cancelled");
        eventService.saveEvent(event);
        return "redirect:/event-organizer/events";
    }
    
    @PostMapping("/events/{eventId}/complete")
    public String completeEvent(@PathVariable Integer eventId) {
        eventService.markEventAsCompleted(eventId);
        return "redirect:/event-organizer/events";
    }

    @PostMapping("/events/{eventId}/delete")
    public String deleteEvent(@PathVariable Integer eventId, Model model) {
        try {
            // First delete all bookings associated with this event
            List<Booking> eventBookings = bookingService.getBookingsByEvent(eventId);
            for (Booking booking : eventBookings) {
                bookingService.deleteBooking(booking.getBookingID());
            }
            
            // Then delete the event
            eventService.saveEvent(eventService.getEventById(eventId));
            eventRepository.deleteById(eventId);
            
            model.addAttribute("success", "Event deleted successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete event: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/event-organizer/events";
    }

    @GetMapping("/bookings")
    public String viewBookings(HttpSession session, Model model, 
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String sort) {
        User currentUser = (User) session.getAttribute("currentUser");
        List<Booking> bookings;
        
        if (search != null && !search.isEmpty()) {
            // Implement search functionality for bookings
            bookings = bookingService.getAllBookings().stream()
                .filter(booking -> booking.getEvent().getEventName().toLowerCase().contains(search.toLowerCase()) ||
                        booking.getPhotographer().getUser().getName().toLowerCase().contains(search.toLowerCase()))
                .toList();
        } else {
            // Get all bookings
            bookings = bookingService.getAllBookings();
        }
        
        // Sort bookings by event date if requested
        if ("date".equals(sort)) {
            bookings = bookings.stream()
                .sorted((b1, b2) -> b1.getEvent().getDate().compareTo(b2.getEvent().getDate()))
                .toList();
        }
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("searchQuery", search);
        model.addAttribute("sort", sort);
        return "event-organizer/bookings";
    }

    @GetMapping("/bookings/{bookingId}")
    public String viewBooking(@PathVariable Integer bookingId, Model model) {
        Booking booking = bookingService.getBookingById(bookingId);
        model.addAttribute("booking", booking);
        return "event-organizer/view-booking";
    }

    @GetMapping("/bookings/new")
    public String showCreateBookingForm(Model model) {
        // Get all events and photographers for selection
        List<Event> events = eventService.getAllEvents();
        List<Photographer> photographers = photographerService.getAllPhotographers();
        
        model.addAttribute("events", events);
        model.addAttribute("photographers", photographers);
        model.addAttribute("booking", new com.eventphotography.management.entity.Booking());
        return "event-organizer/create-booking";
    }

    @PostMapping("/bookings/create")
    public String createBooking(@ModelAttribute com.eventphotography.management.entity.Booking booking,
                               @RequestParam Integer eventId,
                               @RequestParam Integer photographerId,
                               @RequestParam BigDecimal totalAmount,
                               @RequestParam BigDecimal advancePayment,
                               Model model) {
        try {
            // Set the event and photographer
            Event event = new Event();
            event.setEventID(eventId);
            booking.setEvent(event);
            
            Photographer photographer = new Photographer();
            photographer.setPhotographerID(photographerId);
            booking.setPhotographer(photographer);
            
            // Set booking values
            booking.setBookingDate(java.time.LocalDateTime.now());
            booking.setStatus("Pending");
            booking.setTotalAmount(totalAmount);
            booking.setAdvancePayment(advancePayment);
            
            // Save the booking
            bookingService.saveBooking(booking);
            
            model.addAttribute("success", "Booking created successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create booking: " + e.getMessage());
        }
        return "redirect:/event-organizer/bookings";
    }

    @GetMapping("/bookings/{bookingId}/edit")
    public String showEditBookingForm(@PathVariable Integer bookingId, Model model) {
        Booking booking = bookingService.getBookingById(bookingId);
        List<Event> events = eventService.getAllEvents();
        List<Photographer> photographers = photographerService.getAllPhotographers();
        
        model.addAttribute("booking", booking);
        model.addAttribute("events", events);
        model.addAttribute("photographers", photographers);
        return "event-organizer/edit-booking";
    }

    @PostMapping("/bookings/{bookingId}/update")
    public String updateBooking(@PathVariable Integer bookingId,
                               @ModelAttribute Booking booking,
                               @RequestParam Integer eventId,
                               @RequestParam Integer photographerId,
                               Model model) {
        try {
            Booking existingBooking = bookingService.getBookingById(bookingId);
            
            // Update booking details
            existingBooking.setTotalAmount(booking.getTotalAmount());
            existingBooking.setAdvancePayment(booking.getAdvancePayment());
            existingBooking.setStatus(booking.getStatus());
            existingBooking.setNotes(booking.getNotes());
            
            // Update event and photographer
            Event event = new Event();
            event.setEventID(eventId);
            existingBooking.setEvent(event);
            
            Photographer photographer = new Photographer();
            photographer.setPhotographerID(photographerId);
            existingBooking.setPhotographer(photographer);
            
            bookingService.saveBooking(existingBooking);
            
            model.addAttribute("success", "Booking updated successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update booking: " + e.getMessage());
        }
        return "redirect:/event-organizer/bookings";
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public String cancelBooking(@PathVariable Integer bookingId, Model model) {
        try {
            Booking booking = bookingService.getBookingById(bookingId);
            booking.setStatus("Cancelled");
            bookingService.saveBooking(booking);
            model.addAttribute("success", "Booking cancelled successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to cancel booking: " + e.getMessage());
        }
        return "redirect:/event-organizer/bookings";
    }
}