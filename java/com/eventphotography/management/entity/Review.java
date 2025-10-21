package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReviewID")
    private Integer reviewID;

    @ManyToOne
    @JoinColumn(name = "ClientID", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "PhotographerID", nullable = false)
    private Photographer photographer;
    
    @ManyToOne
    @JoinColumn(name = "BookingID")
    private Booking booking;

    @Column(name = "Rating")
    private Integer rating; // 1-5 stars

    @Column(name = "Comment", columnDefinition = "NVARCHAR(500)")
    private String comment;

    @Column(name = "ReviewDate")
    private LocalDateTime reviewDate;

    @Column(name = "Status", columnDefinition = "NVARCHAR(20)")
    private String status; // Active, Inactive, Reported

    // Getter for bookingID that derives from the booking relationship
    public Integer getBookingID() {
        return booking != null ? booking.getBookingID() : null;
    }
    
    // Setter for bookingID that can be used if needed (though not recommended)
    public void setBookingID(Integer bookingID) {
        // This is a no-op since we're using the relationship-based approach
        // The bookingID will be derived from the booking entity
    }
}