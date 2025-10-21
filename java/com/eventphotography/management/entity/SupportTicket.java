package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "SupportTicket")
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TicketID")
    private Integer ticketID;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "BookingID")
    private Booking booking;

    @Column(name = "Subject", columnDefinition = "NVARCHAR(200)")
    private String subject;

    @Column(name = "Description", columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(name = "Status", columnDefinition = "NVARCHAR(20)")
    private String status;

    @Column(name = "Priority", columnDefinition = "NVARCHAR(20)")
    private String priority;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "ResolvedDate")
    private LocalDateTime resolvedDate;

    @ManyToOne
    @JoinColumn(name = "AssignedTo")
    private User assignedTo;
}