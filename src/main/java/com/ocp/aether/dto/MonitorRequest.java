package com.ocp.aether.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MonitorRequest {
    private String id;
    private String url;
    private String encryptedToken;
    private String siteName;
    private LocalDateTime createdAt;
    private String status;
    private String region;
}
