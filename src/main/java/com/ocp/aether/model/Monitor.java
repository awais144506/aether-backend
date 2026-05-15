package com.ocp.aether.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Entity
@Table(name = "monitors")
@Data
public class Monitor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @NotBlank(message = "URL is required")
    @URL(message = "Please provide a valid URL")
    private String url;
    @NotBlank(message = "Region is required")
    private String region;
    @Column(columnDefinition = "TEXT")
    private String encryptedToken;
    private String siteName;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String status = "PENDING";
}
