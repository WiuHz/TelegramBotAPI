package com.example.model;

import java.time.OffsetDateTime;

public class Registration {
    private String userId;
    private String eventId;
    private String status;
    private OffsetDateTime registeredTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getRegisteredTime() {
        return registeredTime;
    }

    public void setRegisteredTime(OffsetDateTime registeredTime) {
        this.registeredTime = registeredTime;
    }
}