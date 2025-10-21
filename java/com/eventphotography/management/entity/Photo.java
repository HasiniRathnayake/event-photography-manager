package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Photo")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PhotoID")
    private Integer photoID;

    @ManyToOne
    @JoinColumn(name = "AlbumID", nullable = false)
    private Album album;

    @Column(name = "FileName", columnDefinition = "NVARCHAR(200)")
    private String fileName;

    @Column(name = "FilePath", columnDefinition = "NVARCHAR(500)")
    private String filePath;

    @Column(name = "ThumbnailPath", columnDefinition = "NVARCHAR(500)")
    private String thumbnailPath;

    @Column(name = "FileSize")
    private Long fileSize;

    @Column(name = "Resolution", columnDefinition = "NVARCHAR(20)")
    private String resolution;

    @Column(name = "CameraMake", columnDefinition = "NVARCHAR(50)")
    private String cameraMake;

    @Column(name = "CameraModel", columnDefinition = "NVARCHAR(50)")
    private String cameraModel;

    @Column(name = "CameraSettings", columnDefinition = "NVARCHAR(200)")
    private String cameraSettings;

    @Column(name = "TakenDateTime")
    private LocalDateTime takenDateTime;

    @Column(name = "UploadedDate")
    private LocalDateTime uploadedDate;

    @Column(name = "IsSelected")
    private Boolean isSelected;

    @Column(name = "IsEdited")
    private Boolean isEdited;
}