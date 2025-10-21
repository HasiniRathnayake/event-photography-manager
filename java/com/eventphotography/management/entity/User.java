package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "\"User\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer userID;

    @Column(name = "Name", columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(name = "Email", columnDefinition = "NVARCHAR(100)")
    private String email;

    @Column(name = "Password", columnDefinition = "NVARCHAR(255)")
    private String password;

    @Column(name = "ContactNo", columnDefinition = "NVARCHAR(15)")
    private String contactNo;

    @Column(name = "Address", columnDefinition = "NVARCHAR(200)")
    private String address;

    @Column(name = "UserType", columnDefinition = "NVARCHAR(20)")
    private String userType;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "IsActive")
    private Boolean isActive;
}