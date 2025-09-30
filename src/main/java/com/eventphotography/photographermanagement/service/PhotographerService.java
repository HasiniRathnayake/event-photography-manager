package com.eventphotography.photographermanagement.service;

import com.eventphotography.photographermanagement.model.*;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhotographerService {

    private final JdbcTemplate jdbcTemplate;
    private final EmailService emailService;

    public PhotographerService(JdbcTemplate jdbcTemplate, EmailService emailService) {
        this.jdbcTemplate = jdbcTemplate;
        this.emailService = emailService;
    }

    public Photographer getPhotographerDetails(int photographerId) {
        String sql = "SELECT p.*, u.Name, u.Email FROM Photographer p JOIN [User] u ON p.UserID = u.UserID WHERE p.PhotographerID = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{photographerId}, new BeanPropertyRowMapper<>(Photographer.class));
    }

    public List<Event> getUpcomingEvents(int photographerId) {
        String sql = "SELECT e.* FROM Event e JOIN Booking b ON e.EventID = b.EventID WHERE b.PhotographerID = ? AND e.Date >= GETDATE() AND b.Status = 'Confirmed' ORDER BY e.Date";
        return jdbcTemplate.query(sql, new Object[]{photographerId}, new BeanPropertyRowMapper<>(Event.class));
    }

    public List<Event> getPastEventsForAlbumCreation(int photographerId) {
        String sql = "SELECT e.* FROM Event e JOIN Booking b ON e.EventID = b.EventID LEFT JOIN Album a ON e.EventID = a.EventID WHERE b.PhotographerID = ? AND e.Status = 'Completed' AND a.AlbumID IS NULL";
        return jdbcTemplate.query(sql, new Object[]{photographerId}, new BeanPropertyRowMapper<>(Event.class));
    }

    public List<Review> getRecentReviews(int photographerId) {
        String sql = "SELECT TOP 5 r.*, c.Name as clientName FROM Review r JOIN Client cl ON r.ClientID = cl.ClientID JOIN [User] c ON cl.UserID = c.UserID WHERE r.PhotographerID = ? ORDER BY r.ReviewDate DESC";
        return jdbcTemplate.query(sql, new Object[]{photographerId}, (rs, rowNum) -> {
            Review review = new Review();
            review.setReviewID(rs.getInt("ReviewID"));
            review.setRating(rs.getInt("Rating"));
            review.setComment(rs.getString("Comment"));
            review.setReviewDate(rs.getDate("ReviewDate"));
            review.setClientName(rs.getString("clientName"));
            return review;
        });
    }

    public List<Album> getAlbumsByPhotographer(int photographerId) {
        String sql = "SELECT a.* FROM Album a JOIN Event e ON a.EventID = e.EventID JOIN Booking b ON e.EventID = b.EventID WHERE b.PhotographerID = ? ORDER BY a.CreatedDate DESC";
        return jdbcTemplate.query(sql, new Object[]{photographerId}, new BeanPropertyRowMapper<>(Album.class));
    }

    public void createAlbum(Album album, int photographerId) {
        String sql = "INSERT INTO Album (EventID, AlbumName, Description) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, album.getEventID(), album.getAlbumName(), album.getDescription());
    }

    public void savePhoto(int albumId, String fileName, String filePath) {
        String sql = "INSERT INTO Photo (AlbumID, FileName, FilePath) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, albumId, fileName, filePath);
    }

    // Email Notification Methods
    public void notifyNewBooking(int photographerId) {
        Photographer photographer = getPhotographerDetails(photographerId);
        String subject = "New Event Booking!";
        String text = "Hello " + photographer.getName() + ",\n\nYou have a new event booking. Please log in to your dashboard to view the details.";
        emailService.sendSimpleMessage(photographer.getEmail(), subject, text);
    }

    public void notifyUpcomingEvent(int photographerId, Event event) {
        Photographer photographer = getPhotographerDetails(photographerId);
        String subject = "Upcoming Event Reminder: " + event.getEventName();
        String text = "Hello " + photographer.getName() + ",\n\nThis is a reminder for your upcoming event '" + event.getEventName() + "' on " + event.getDate() + " at " + event.getVenue() + ".";
        emailService.sendSimpleMessage(photographer.getEmail(), subject, text);
    }

    public void notifyNewReview(int photographerId, Review review) {
        Photographer photographer = getPhotographerDetails(photographerId);
        String subject = "You've Received a New Review!";
        String text = "Hello " + photographer.getName() + ",\n\nA client has left a new review for one of your events. Log in to see what they said!";
        emailService.sendSimpleMessage(photographer.getEmail(), subject, text);
    }

    public void remindToCreateAlbum(int photographerId, Event event) {
        Photographer photographer = getPhotographerDetails(photographerId);
        String subject = "Reminder: Create Album for " + event.getEventName();
        String text = "Hello " + photographer.getName() + ",\n\nDon't forget to create an album and upload photos for the completed event: '" + event.getEventName() + "'.";
        emailService.sendSimpleMessage(photographer.getEmail(), subject, text);
    }
}
