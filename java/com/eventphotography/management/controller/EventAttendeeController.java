package com.eventphotography.management.controller;

import com.eventphotography.management.entity.*;
import com.eventphotography.management.repository.AlbumRepository;
import com.eventphotography.management.repository.ClientRepository;
import com.eventphotography.management.repository.EventRepository;
import com.eventphotography.management.service.BookingService;
import com.eventphotography.management.service.EventService;
import com.eventphotography.management.service.PhotoService;
import com.eventphotography.management.service.SupportService;
import com.eventphotography.management.service.ReviewService;
import com.eventphotography.management.service.PhotographerService;
import com.eventphotography.management.service.UserService;
import com.eventphotography.management.util.UserTypeChecker;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/attendee")
public class EventAttendeeController {

    @Autowired
    private EventService eventService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private SupportService supportService;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private PhotographerService photographerService;

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isEventAttendee(currentUser)) {
            return "redirect:/auth/login";
        }

        // Get all upcoming events
        List<Event> upcomingEventsList = eventService.getUpcomingEvents();
        
        // Get the client entity for the current user
        Client client = clientRepository.findByUserId(currentUser.getUserID());
        List<Album> recentAlbums = new ArrayList<>();
        
        // Initialize counts
        long totalEvents = 0;
        long upcomingEvents = upcomingEventsList.size();
        long availableAlbums = 0;
        
        // Get all events in the system for total events count
        List<Event> allEvents = eventService.getAllEvents();
        totalEvents = allEvents.size();
        
        if (client != null) {
            System.out.println("Found client with ID: " + client.getClientID());
            // Directly get albums for this client using the repository method
            recentAlbums = albumRepository.findByClientId(client.getClientID());
            System.out.println("Found " + recentAlbums.size() + " albums for client ID: " + client.getClientID());
            
            // Calculate photo count for each album
            for (Album album : recentAlbums) {
                List<Photo> photos = photoService.getPhotosByAlbum(album.getAlbumID());
                album.setPhotoCount(photos.size());
            }
            
            // Print details of each album for debugging
            for (Album album : recentAlbums) {
                System.out.println("  Album: " + album.getAlbumName() + " (ID: " + album.getAlbumID() + ")");
                if (album.getEvent() != null) {
                    System.out.println("    Event: " + album.getEvent().getEventName() + " (ID: " + album.getEvent().getEventID() + ")");
                }
            }
            
            // Limit to 5 most recent albums
            recentAlbums = recentAlbums.stream()
                    .sorted((a1, a2) -> a2.getCreatedDate().compareTo(a1.getCreatedDate()))
                    .limit(5)
                    .toList();
        } else {
            System.out.println("No client found for user ID: " + currentUser.getUserID());
        }
        
        // Also get all public albums for the attendee to browse
        List<Album> allAlbums = albumRepository.findAll();
        System.out.println("Total albums in system: " + allAlbums.size());
        
        List<Album> publicAlbums = allAlbums.stream()
                .filter(album -> {
                    boolean isPublic = album.getIsPublic() != null && album.getIsPublic();
                    System.out.println("Album " + album.getAlbumName() + " (ID: " + album.getAlbumID() + ") isPublic: " + isPublic + ", isPublic value: " + album.getIsPublic());
                    return isPublic;
                })
                .sorted((a1, a2) -> a2.getCreatedDate().compareTo(a1.getCreatedDate()))
                .limit(5) // Limit to 5 most recent public albums
                .toList();
        
        // Calculate photo count for public albums
        for (Album album : publicAlbums) {
            List<Photo> photos = photoService.getPhotosByAlbum(album.getAlbumID());
            album.setPhotoCount(photos.size());
        }
        
        // Calculate available albums (client albums + public albums)
        availableAlbums = recentAlbums.size() + publicAlbums.size();
        
        // Get all photographers for the rating dropdown (not just approved ones)
        List<Photographer> photographers = photographerService.getAllPhotographers();

        model.addAttribute("upcomingEventsList", upcomingEventsList);
        model.addAttribute("recentAlbums", recentAlbums);
        model.addAttribute("publicAlbums", publicAlbums);
        model.addAttribute("photographers", photographers);
        
        // Add counts for dashboard metrics
        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("availableAlbums", availableAlbums);
        return "event-attendee/dashboard";
    }

    @GetMapping("/events")
    public String viewEvents(Model model) {
        List<Event> events = eventService.getPublicEvents();
        model.addAttribute("events", events);
        return "event-attendee/events";
    }

    @GetMapping("/events/{eventId}/photos")
    public String viewEventPhotos(@PathVariable Integer eventId, Model model) {
        Event event = eventService.getEventById(eventId);
        List<Photo> photos = photoService.getPhotosByEvent(eventId);

        model.addAttribute("event", event);
        model.addAttribute("photos", photos);
        return "event-attendee/photos";
    }

    @GetMapping("/photos/{photoId}/download")
    public ResponseEntity<Resource> downloadPhoto(@PathVariable Integer photoId) {
        try {
            Photo photo = photoService.getPhotoById(photoId);
            Path filePath = Paths.get(photo.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + photo.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/photos/{photoId}/preview")
    public ResponseEntity<Resource> previewPhoto(@PathVariable Integer photoId) {
        try {
            Photo photo = photoService.getPhotoById(photoId);
            Path filePath = Paths.get(photo.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/photos/{photoId}/report")
    public String reportPhoto(@PathVariable Integer photoId,
                              @RequestParam String reason,
                              HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        // Implementation for reporting photos (would create a support ticket)
        return "redirect:/attendee/photos";
    }

    @PostMapping("/photographers/rate")
    public String ratePhotographer(@RequestParam Integer photographerId,
                                   @RequestParam Integer rating,
                                   @RequestParam String comment,
                                   HttpSession session,
                                   Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isEventAttendee(currentUser)) {
            return "redirect:/auth/login";
        }
        
        try {
            System.out.println("=== Starting photographer rating process ===");
            System.out.println("Rating photographer ID: " + photographerId + " with rating: " + rating);
            
            // Get the client entity for the current user
            Client client = clientRepository.findByUserId(currentUser.getUserID());
            System.out.println("Current user ID: " + currentUser.getUserID());
            System.out.println("Found client: " + (client != null ? client.getClientID() : "null"));
            
            if (client == null) {
                // If client doesn't exist, create a client entity for this user
                System.out.println("Client entity not found for user ID: " + currentUser.getUserID() + ", creating one...");
                userService.createClientForUser(currentUser);
                // Try to get the client entity again
                client = clientRepository.findByUserId(currentUser.getUserID());
                System.out.println("After creation, found client: " + (client != null ? client.getClientID() : "null"));
                if (client == null) {
                    System.out.println("Failed to create client entity for user ID: " + currentUser.getUserID());
                    // If we still can't get the client, create a minimal client object
                    client = new Client();
                    client.setClientID(currentUser.getUserID());
                    System.out.println("Created minimal client object");
                } else {
                    System.out.println("Successfully created client entity for user ID: " + currentUser.getUserID());
                }
            }
            
            // Get the photographer
            Photographer photographer = photographerService.getPhotographerDetails(photographerId);
            System.out.println("Found photographer: " + (photographer != null ? photographer.getUser().getName() : "null"));
            
            // Find the relevant booking for this client and photographer
            Booking booking = null;
            if (client != null && photographer != null) {
                List<Booking> bookings = bookingService.getBookingsByClientIdAndPhotographerId(client.getClientID(), photographerId);
                if (!bookings.isEmpty()) {
                    // Use the first booking found
                    booking = bookings.get(0);
                    System.out.println("Found booking ID: " + booking.getBookingID());
                } else {
                    System.out.println("No booking found for client ID: " + client.getClientID() + " and photographer ID: " + photographerId);
                }
            }
            
            // Create the review
            Review review = new Review();
            review.setClient(client);
            review.setPhotographer(photographer);
            review.setRating(rating);
            review.setComment(comment);
            review.setReviewDate(java.time.LocalDateTime.now());
            review.setStatus("Active");
            // Set the booking if found
            if (booking != null) {
                review.setBooking(booking);
                System.out.println("Setting booking entity with ID: " + booking.getBookingID());
            } else {
                System.out.println("No booking found, review will be created without booking reference");
            }
            
            System.out.println("Creating review with:");
            System.out.println("  Client ID: " + (client != null ? client.getClientID() : "null"));
            System.out.println("  Photographer ID: " + (photographer != null ? photographer.getPhotographerID() : "null"));
            System.out.println("  Booking ID: " + (booking != null ? booking.getBookingID() : "null"));
            System.out.println("  Rating: " + rating);
            System.out.println("  Comment: " + comment);
            
            // Save the review
            Review savedReview = reviewService.createReview(review);
            System.out.println("Created review with ID: " + (savedReview != null ? savedReview.getReviewID() : "null"));
            if (savedReview != null) {
                System.out.println("Saved review BookingID: " + savedReview.getBookingID());
            }
            
            // Update photographer's average rating
            System.out.println("Updating photographer rating...");
            photographerService.updatePhotographerRating(photographerId);
            
            // Notify photographer about new review
            System.out.println("Notifying photographer about new review...");
            photographerService.notifyNewReview(photographerId, savedReview);
            
            model.addAttribute("success", "Thank you for your review!");
            System.out.println("=== Completed photographer rating process ===");
        } catch (Exception e) {
            System.out.println("Error rating photographer: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to submit review: " + e.getMessage());
        }
        
        return "redirect:/attendee/dashboard";
    }

    @PostMapping("/events/{eventId}/feedback")
    public String submitFeedback(@PathVariable Integer eventId,
                                 @RequestParam Integer rating,
                                 @RequestParam String comment,
                                 HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        // Implementation for submitting feedback (would create a review)
        return "redirect:/attendee/events/" + eventId + "/photos";
    }

    @GetMapping("/search")
    public String searchEvents(@RequestParam String query, Model model) {
        List<Event> events = eventService.getPublicEvents().stream()
                .filter(event -> event.getEventName().toLowerCase().contains(query.toLowerCase()) ||
                        event.getDescription().toLowerCase().contains(query.toLowerCase()))
                .toList();
        model.addAttribute("events", events);
        model.addAttribute("searchQuery", query);
        return "event-attendee/search-results";
    }

    @GetMapping("/photos")
    public String viewMyPhotos(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isEventAttendee(currentUser)) {
            return "redirect:/auth/login";
        }
        
        // Get the client entity for the current user
        Client client = clientRepository.findByUserId(currentUser.getUserID());
        List<Album> clientAlbums = new ArrayList<>();
        List<Photo> clientPhotos = new ArrayList<>();
        
        if (client != null) {
            // Get events for this client
            List<Event> clientEvents = eventRepository.findEventsByClientId(client.getClientID());
            
            // Get albums for these events
            for (Event event : clientEvents) {
                List<Album> eventAlbums = albumRepository.findByEvent_EventID(event.getEventID());
                clientAlbums.addAll(eventAlbums);
                
                // Calculate photo count for each album
                for (Album album : eventAlbums) {
                    List<Photo> photos = photoService.getPhotosByAlbum(album.getAlbumID());
                    album.setPhotoCount(photos.size());
                }
            }
            
            // Get all photos for these events
            for (Event event : clientEvents) {
                List<Photo> eventPhotos = photoService.getPhotosByEvent(event.getEventID());
                clientPhotos.addAll(eventPhotos);
            }
        }
        
        // Also get all public albums for the attendee to browse
        List<Album> allAlbums = albumRepository.findAll();
        List<Album> publicAlbums = allAlbums.stream()
                .filter(album -> album.getIsPublic() != null && album.getIsPublic())
                .sorted((a1, a2) -> a2.getCreatedDate().compareTo(a1.getCreatedDate()))
                .collect(Collectors.toList());
        
        // Calculate photo count for public albums
        for (Album album : publicAlbums) {
            List<Photo> photos = photoService.getPhotosByAlbum(album.getAlbumID());
            album.setPhotoCount(photos.size());
        }
        
        // Get all public photos
        List<Photo> publicPhotos = new ArrayList<>();
        for (Album album : publicAlbums) {
            List<Photo> albumPhotos = photoService.getPhotosByAlbum(album.getAlbumID());
            publicPhotos.addAll(albumPhotos);
        }

        model.addAttribute("clientAlbums", clientAlbums);
        model.addAttribute("clientPhotos", clientPhotos);
        model.addAttribute("publicAlbums", publicAlbums);
        model.addAttribute("publicPhotos", publicPhotos);
        return "event-attendee/photos";
    }
    
    @GetMapping("/my-photos")
    public String viewMyPhotosOld(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        // Implementation to get photos that the attendee has purchased or has access to
        return "event-attendee/my-photos";
    }
    
    @GetMapping("/albums/{albumId}")
    public String viewAlbum(@PathVariable Integer albumId,
                           HttpSession session,
                           Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isEventAttendee(currentUser)) {
            return "redirect:/auth/login";
        }
        
        // Get the album
        Album album = albumRepository.findById(albumId).orElse(null);
        if (album == null) {
            System.out.println("Album not found with ID: " + albumId);
            return "redirect:/attendee/photos";
        }
        
        System.out.println("Found album: " + album.getAlbumName() + " (ID: " + album.getAlbumID() + ")");
        System.out.println("Album isPublic: " + album.getIsPublic() + ", isPublic value: " + album.getIsPublic());
        
        // Check if the user can access this album
        // Users can access their own albums or public albums
        boolean canAccess = false;
        Client client = clientRepository.findByUserId(currentUser.getUserID());
        
        if (album.getIsPublic() != null && album.getIsPublic()) {
            // Public album - anyone can access
            canAccess = true;
            System.out.println("Granting access to public album");
        } else if (client != null) {
            // Check if this is the client's album
            if (album.getEvent().getClient().getClientID().equals(client.getClientID())) {
                canAccess = true;
                System.out.println("Granting access to client's album");
            } else {
                System.out.println("Client ID mismatch. Album client ID: " + album.getEvent().getClient().getClientID() + ", User client ID: " + client.getClientID());
            }
        } else {
            System.out.println("No client found for user or album is not public");
        }
        
        if (!canAccess) {
            System.out.println("Access denied to album ID: " + albumId);
            return "redirect:/attendee/photos";
        }
        
        // Get photos for this album
        List<Photo> photos = photoService.getPhotosByAlbum(albumId);
        System.out.println("Found " + photos.size() + " photos for album ID: " + albumId);
        
        model.addAttribute("album", album);
        model.addAttribute("photos", photos);
        return "event-attendee/album-view";
    }
    
    @GetMapping("/tickets/create")
    public String showCreateTicketForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isEventAttendee(currentUser)) {
            return "redirect:/auth/login";
        }
        
        // Get all events
        List<Event> allEvents = eventService.getAllEvents();
        model.addAttribute("events", allEvents);
        model.addAttribute("ticket", new SupportTicket());
        return "event-attendee/create-ticket";
    }

    @PostMapping("/tickets/create")
    public String createTicket(@RequestParam String subject,
                               @RequestParam String description,
                               @RequestParam(required = false) Integer eventId,
                               HttpSession session,
                               Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isEventAttendee(currentUser)) {
            return "redirect:/auth/login";
        }
        
        try {
            // Create the ticket
            SupportTicket ticket = supportService.createTicket(currentUser.getUserID(), subject, description, eventId);
            model.addAttribute("success", "Ticket created successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create ticket: " + e.getMessage());
            // Get all events
            List<Event> allEvents = eventService.getAllEvents();
            model.addAttribute("events", allEvents);
            model.addAttribute("ticket", new SupportTicket());
            return "event-attendee/create-ticket";
        }
        
        return "redirect:/attendee/dashboard";
    }
}