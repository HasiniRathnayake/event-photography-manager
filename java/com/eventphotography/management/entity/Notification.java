package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Integer notificationID;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "Title", columnDefinition = "NVARCHAR(200)")
    private String title;

    @Column(name = "Message", columnDefinition = "NVARCHAR(1000)")
    private String message;

    @Column(name = "Type", columnDefinition = "NVARCHAR(50)")
    private String type;

    @Column(name = "IsRead")
    private Boolean isRead;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "RelatedID")
    private Integer relatedID;
}