package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MessageID")
    private Integer messageID;

    @ManyToOne
    @JoinColumn(name = "SenderID", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "ReceiverID", nullable = false)
    private User receiver;

    @Column(name = "Subject", columnDefinition = "NVARCHAR(200)")
    private String subject;

    @Column(name = "Content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "SentDate")
    private LocalDateTime sentDate;

    @Column(name = "IsRead")
    private Boolean isRead = false;

    @Column(name = "MessageType", columnDefinition = "NVARCHAR(20)")
    private String messageType; // General, Booking, Support, Notification

    @Column(name = "Status", columnDefinition = "NVARCHAR(20)")
    private String status; // Sent, Delivered, Read, Archived, Deleted
}