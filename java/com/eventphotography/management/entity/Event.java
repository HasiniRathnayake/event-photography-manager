package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EventID")
    private Integer eventID;

    @ManyToOne
    @JoinColumn(name = "ClientID", nullable = false)
    private Client client;

    @Column(name = "EventName", columnDefinition = "NVARCHAR(100)")
    private String eventName;

    @Column(name = "EventType", columnDefinition = "NVARCHAR(50)")
    private String eventType;

    @Column(name = "Date")
    private LocalDate date;

    @Column(name = "StartTime")
    private LocalTime startTime;

    @Column(name = "EndTime")
    private LocalTime endTime;

    @Column(name = "Venue", columnDefinition = "NVARCHAR(200)")
    private String venue;

    @Column(name = "Description", columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(name = "ExpectedGuests")
    private Integer expectedGuests;

    @Column(name = "SpecialRequirements", columnDefinition = "NVARCHAR(500)")
    private String specialRequirements;

    @Column(name = "Status", columnDefinition = "NVARCHAR(20)")
    private String status;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;
}
