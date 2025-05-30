package com.example.model;

import com.example.model.Event;
import com.example.model.User;

import java.util.List;

public class UserRegistrationState {
    private User user;
    private Event event;
    private List<String> eventIds;
    private UserRegistrationStep step;
    private String airtableRecordId; // RecordId for Airtable
    private Event originalEvent; // Store original event for comparison

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<String> getEventIds() {
        return eventIds;
    }

    public void setEventIds(List<String> eventIds) {
        this.eventIds = eventIds;
    }

    public UserRegistrationStep getStep() {
        return step;
    }

    public void setStep(UserRegistrationStep step) {
        this.step = step;
    }

    public String getAirtableRecordId() {
        return airtableRecordId;
    }

    public void setAirtableRecordId(String airtableRecordId) {
        this.airtableRecordId = airtableRecordId;
    }

    public Event getOriginalEvent() {
        return originalEvent;
    }

    public void setOriginalEvent(Event originalEvent) {
        this.originalEvent = originalEvent;
    }
}