package com.muvix.domain.model.security;

import com.muvix.domain.model.exception.DomainException;
import java.time.Instant;

public class AuditLog {

    private final Long id;
    private final Long userId;
    private final String action;
    private final String resource;
    private final String details;
    private final String ipAddress;
    private final Instant timestamp;

    public AuditLog(Long id, Long userId, String action, String resource, String details, String ipAddress, Instant timestamp) {
        if (action == null || action.isBlank()) throw new DomainException("Audit action is required");
        if (resource == null || resource.isBlank()) throw new DomainException("Audit resource is required");

        this.id = id;
        this.userId = userId;
        this.action = action;
        this.resource = resource;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getAction() { return action; }
    public String getResource() { return resource; }
    public String getDetails() { return details; }
    public String getIpAddress() { return ipAddress; }
    public Instant getTimestamp() { return timestamp; }
}
