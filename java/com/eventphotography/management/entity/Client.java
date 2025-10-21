package com.eventphotography.management.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "Client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClientID")
    private Integer clientID;

    @OneToOne
    @JoinColumn(name = "UserID")
    private User user;

    @Column(name = "CompanyName", columnDefinition = "NVARCHAR(100)")
    private String companyName;

    @Column(name = "PreferredStyle", columnDefinition = "NVARCHAR(50)")
    private String preferredStyle;

    @Column(name = "Budget")
    private BigDecimal budget;
}