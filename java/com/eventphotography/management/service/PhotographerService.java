package com.eventphotography.management.service;

import com.eventphotography.management.entity.*;
import com.eventphotography.management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class PhotographerService {

    @Autowired
    private PhotographerRepository photographerRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    // Removed EmailService dependency since we're removing email functionality
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;

    @PostConstruct
    @Transactional
    public void initializeAlbums() {
        try {
            System.out.println("=== Starting album initialization ===");
            // Make all existing albums public so attendees can see them
            List<Album> allAlbums = albumRepository.findAll();
            System.out.println("Found " + allAlbums.size() + " albums to process");
            int updatedCount = 0;
            for (Album album : allAlbums) {
                if (album.getIsPublic() == null || !album.getIsPublic()) {
                    album.setIsPublic(true);
                    albumRepository.save(album);
                    System.out.println("Made album public: " + album.getAlbumName() + " (ID: " + album.getAlbumID() + ")");
                    updatedCount++;
                } else {
                    System.out.println("Album already public: " + album.getAlbumName() + " (ID: " + album.getAlbumID() + ")");
                }
            }
            System.out.println("Updated " + updatedCount + " albums to public");
            System.out.println("=== Completed album initialization ===");
        } catch (Exception e) {
            System.err.println("Error in initializeAlbums: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Photographer getPhotographerByUser(User user) {
        // Debug logging to see what's happening
        System.out.println("Looking for photographer with user ID: " + user.getUserID());
        Photographer photographer = photographerRepository.findByUserId(user.getUserID());
        System.out.println("Found photographer: " + photographer);
        if (photographer == null) {
            System.out.println("No photographer found for user ID: " + user.getUserID());
            // Let's also check if there are any photographers in the database
            long photographerCount = photographerRepository.count();
            System.out.println("Total photographers in database: " + photographerCount);
            // Let's also check all photographers
            List<Photographer> allPhotographers = photographerRepository.findAll();
            System.out.println("All photographers:");
            for (Photographer p : allPhotographers) {
                System.out.println("  Photographer ID: " + p.getPhotographerID() + ", User ID: " + p.getUser().getUserID() + ", Name: " + p.getUser().getName());
            }
        } else {
            System.out.println("Photographer found: ID=" + photographer.getPhotographerID() + ", User ID=" + photographer.getUser().getUserID() + ", Name=" + photographer.getUser().getName() + ", Rating=" + photographer.getRating());
        }
        return photographer;
    }

    public Photographer getPhotographerDetails(Integer photographerId) {
        System.out.println("Getting photographer details for ID: " + photographerId);
        Optional<Photographer> photographer = photographerRepository.findById(photographerId);
        if (photographer.isPresent()) {
            System.out.println("Found photographer: " + photographer.get().getUser().getName());
        } else {
            System.out.println("Photographer not found for ID: " + photographerId);
        }
        return photographer.orElseThrow(() -> new RuntimeException("Photographer not found"));
    }

    public List<Event> getUpcomingEvents(Integer photographerId) {
        return eventRepository.findUpcomingEventsByPhotographer(photographerId);
    }

    public List<Event> getPastEventsForAlbumCreation(Integer photographerId) {
        return eventRepository.findPastEventsForAlbumCreation(photographerId);
    }
    
    // New method to get all events for album creation (past and current)
    public List<Event> getAllEventsForAlbumCreation(Integer photographerId) {
        return eventRepository.findAllEventsForPhotographer(photographerId);
    }

    public List<Review> getRecentReviews(Integer photographerId) {
        return reviewRepository.findTop5ByPhotographer_PhotographerIDOrderByReviewDateDesc(photographerId);
    }

    public List<Album> getAlbumsByPhotographer(Integer photographerId) {
        List<Album> albums = albumRepository.findByPhotographerId(photographerId);
        System.out.println("Found " + albums.size() + " albums for photographer ID: " + photographerId);
        return albums;
    }

    public void createAlbum(Album album, Integer photographerId) {
        // Verify that the photographer has access to this event
        Event event = eventRepository.findById(album.getEvent().getEventID())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Check if photographer is assigned to this event
        boolean isAssigned = bookingRepository.findByEventId(event.getEventID()).stream()
                .anyMatch(booking -> booking.getPhotographer().getPhotographerID().equals(photographerId));

        if (!isAssigned) {
            throw new RuntimeException("Photographer not assigned to this event");
        }
        
        // Set album as public by default so attendees can see it
        if (album.getIsPublic() == null) {
            album.setIsPublic(true);
            System.out.println("Setting album as public by default");
        } else {
            System.out.println("Album isPublic already set to: " + album.getIsPublic());
        }
        
        System.out.println("Creating album: " + album.getAlbumName() + " for event: " + event.getEventName());
        System.out.println("Album isPublic: " + album.getIsPublic());

        Album savedAlbum = albumRepository.save(album);
        System.out.println("Album saved with ID: " + savedAlbum.getAlbumID() + " and isPublic: " + savedAlbum.getIsPublic());
    }

    public void savePhoto(Integer albumId, String fileName, String filePath) {
        Photo photo = new Photo();
        Album album = new Album();
        album.setAlbumID(albumId);
        photo.setAlbum(album);
        photo.setFileName(fileName);
        photo.setFilePath(filePath);
        photo.setUploadedDate(java.time.LocalDateTime.now());
        System.out.println("Saving photo: " + fileName + " to album ID: " + albumId);
        photoRepository.save(photo);
        System.out.println("Photo saved with ID: " + photo.getPhotoID());
    }

    // New method to set cover photo for an album
    public void setAlbumCoverPhoto(Integer albumId, Integer photoId) {
        Album album = albumRepository.findById(albumId).orElse(null);
        if (album != null) {
            // Verify that the photo belongs to this album
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo != null && photo.getAlbum().getAlbumID().equals(albumId)) {
                album.setCoverPhotoID(photoId);
                albumRepository.save(album);
            }
        }
    }

    public void updatePhotographer(Photographer photographer) {
        System.out.println("Updating photographer: " + photographer.getUser().getName());
        System.out.println("New rating: " + photographer.getRating());
        photographerRepository.save(photographer);
        System.out.println("Photographer updated successfully");
    }

    // Get all photographers
    public List<Photographer> getAllPhotographers() {
        return photographerRepository.findAll();
    }

    // Notification methods (email removed, only database notifications)
    public void notifyNewBooking(Integer photographerId, Integer bookingId) {
        System.out.println("Notifying photographer ID: " + photographerId + " about booking ID: " + bookingId);
        Photographer photographer = photographerRepository.findById(photographerId).orElse(null);
        if (photographer != null) {
            User user = photographer.getUser();
            System.out.println("Found photographer user: " + user.getName());
            // Create a database notification
            notificationService.notifyNewBooking(user, bookingId);
        } else {
            System.out.println("Photographer not found for ID: " + photographerId);
        }
    }

    public void notifyUpcomingEvent(Integer photographerId, Event event) {
        Photographer photographer = photographerRepository.findById(photographerId).orElse(null);
        if (photographer != null) {
            User user = photographer.getUser();
            // Create a database notification
            notificationService.notifyEventUpdate(user, "Upcoming event reminder: " + event.getEventName(), event.getEventID());
        }
    }

    public void notifyNewReview(Integer photographerId, Review review) {
        Photographer photographer = photographerRepository.findById(photographerId).orElse(null);
        if (photographer != null) {
            User user = photographer.getUser();
            
            // Try to get event name from booking if booking is available
            String eventName = "";
            if (review.getBooking() != null) {
                Booking booking = review.getBooking();
                if (booking.getEvent() != null) {
                    eventName = " for event '" + booking.getEvent().getEventName() + "'";
                }
            } else if (review.getBookingID() != null) {
                // Fallback to bookingID if booking entity is not set
                Booking booking = bookingRepository.findById(review.getBookingID()).orElse(null);
                if (booking != null && booking.getEvent() != null) {
                    eventName = " for event '" + booking.getEvent().getEventName() + "'";
                }
            }
            
            // Create a database notification
            notificationService.notifyReviewUpdate(user, "You've received a new review with " + review.getRating() + " stars" + eventName + ".", review.getReviewID());
        }
    }

    public void remindToCreateAlbum(Integer photographerId, Event event) {
        Photographer photographer = photographerRepository.findById(photographerId).orElse(null);
        if (photographer != null) {
            User user = photographer.getUser();
            // Create a database notification
            notificationService.notifyEventUpdate(user, "Reminder: Create album for " + event.getEventName(), event.getEventID());
        }
    }

    public void notifyAlbumCreated(Event event) {
        // Notify the client (event organizer) associated with this event
        Client client = event.getClient();
        if (client != null) {
            User user = client.getUser();
            if (user != null) {
                System.out.println("Sending album created notification to client: " + user.getName() + " (ID: " + user.getUserID() + ")");
                // Create a database notification with valid type
                notificationService.notifyAlbumUpdate(user, "A new photo album has been created for your event '" + event.getEventName() + "'.", event.getEventID());
            }
        } else {
            System.out.println("No client found for event: " + event.getEventName());
        }
    }

    /**
     * Notify all event organizers (clients) about album creation
     * This method can be used to notify all users with CLIENT user type
     */
    public void notifyAllEventOrganizers(Event event) {
        // Get all users with CLIENT user type
        List<User> clientUsers = userService.getUsersByUserType("CLIENT");
        System.out.println("Found " + clientUsers.size() + " event organizers to notify about album creation");
        
        for (User user : clientUsers) {
            System.out.println("Sending album created notification to event organizer: " + user.getName() + " (ID: " + user.getUserID() + ")");
            // Create a database notification with valid type
            notificationService.notifyAlbumUpdate(user, "A new photo album has been created for event '" + event.getEventName() + "'.", event.getEventID());
        }
    }

    public void notifyPhotosUploaded(Event event) {
        // Notify the client (event organizer) associated with this event
        Client client = event.getClient();
        if (client != null) {
            User user = client.getUser();
            if (user != null) {
                System.out.println("Sending photos uploaded notification to client: " + user.getName() + " (ID: " + user.getUserID() + ")");
                // Create a database notification with valid type
                notificationService.notifyAlbumUpdate(user, "New photos have been uploaded to your event album '" + event.getEventName() + "'.", event.getEventID());
            }
        } else {
            System.out.println("No client found for event: " + event.getEventName());
        }
    }
    
    @Transactional
    public void updatePhotographerRating(Integer photographerId) {
        System.out.println("=== Starting photographer rating update ===");
        System.out.println("Updating rating for photographer ID: " + photographerId);
        try {
            Photographer photographer = photographerRepository.findById(photographerId).orElse(null);
            if (photographer != null) {
                System.out.println("Found photographer: " + photographer.getUser().getName());
                System.out.println("Current rating: " + photographer.getRating());
                List<Review> allReviews = reviewRepository.findByPhotographer_PhotographerID(photographerId);
                System.out.println("Found " + allReviews.size() + " total reviews");
                
                // Filter for active reviews
                List<Review> activeReviews = allReviews.stream()
                    .filter(review -> "Active".equals(review.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
                System.out.println("Found " + activeReviews.size() + " active reviews");
                
                if (!activeReviews.isEmpty()) {
                    // Calculate average rating
                    double sum = 0;
                    for (Review review : activeReviews) {
                        sum += review.getRating();
                        System.out.println("Review ID " + review.getReviewID() + " rating: " + review.getRating());
                    }
                    double averageRating = sum / activeReviews.size();
                    System.out.println("Calculated average rating: " + averageRating);
                    System.out.println("Sum of ratings: " + sum);
                    System.out.println("Number of active reviews: " + activeReviews.size());
                    
                    // Update photographer rating
                    photographer.setRating(java.math.BigDecimal.valueOf(averageRating));
                    System.out.println("Setting new rating: " + photographer.getRating());
                    photographerRepository.save(photographer);
                    System.out.println("Saved photographer with new rating: " + photographer.getRating());
                    
                    // Verify the rating was saved
                    Photographer updatedPhotographer = photographerRepository.findById(photographerId).orElse(null);
                    if (updatedPhotographer != null) {
                        System.out.println("Verified rating after save: " + updatedPhotographer.getRating());
                    }
                } else {
                    System.out.println("No active reviews found, setting rating to 0.0");
                    // Set rating to 0.0 if no active reviews
                    photographer.setRating(java.math.BigDecimal.ZERO);
                    photographerRepository.save(photographer);
                    System.out.println("Saved photographer with zero rating");
                }
            } else {
                System.out.println("Photographer not found for ID: " + photographerId);
            }
            System.out.println("=== Completed photographer rating update ===");
        } catch (Exception e) {
            System.out.println("Error updating photographer rating: " + e.getMessage());
            e.printStackTrace();
        }
    }
}