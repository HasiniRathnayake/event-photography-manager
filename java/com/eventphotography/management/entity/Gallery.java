package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Gallery")
public class Gallery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GalleryID")
    private Integer galleryID;

    @ManyToOne
    @JoinColumn(name = "PhotographerID", nullable = false)
    private Photographer photographer;

    @Column(name = "Title", columnDefinition = "NVARCHAR(100)")
    private String title;

    @Column(name = "Description", columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(name = "CoverImagePath", columnDefinition = "NVARCHAR(500)")
    private String coverImagePath;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "IsPublic")
    private Boolean isPublic = true;

    @Column(name = "Status", columnDefinition = "NVARCHAR(20)")
    private String status; // Active, Inactive
}