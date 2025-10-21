package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Album")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AlbumID")
    private Integer albumID;

    @ManyToOne
    @JoinColumn(name = "EventID", nullable = false)
    private Event event;

    @Column(name = "AlbumName", columnDefinition = "NVARCHAR(100)")
    private String albumName;

    @Column(name = "Description", columnDefinition = "NVARCHAR(300)")
    private String description;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "CoverPhotoID")
    private Integer coverPhotoID;

    @Column(name = "IsPublic")
    private Boolean isPublic;

    @Column(name = "ShareLink", columnDefinition = "NVARCHAR(200)")
    private String shareLink;
    
    @Transient
    private Integer photoCount;
}