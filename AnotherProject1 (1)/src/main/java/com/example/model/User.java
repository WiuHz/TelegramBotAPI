package com.example.model;

import java.time.OffsetDateTime;

public class User {
    private String fullName;
    private String phoneNumber;
    private String email;
    private Long telegramId;
    private OffsetDateTime dateRegistered;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getTelegramId() { return telegramId; }
    public void setTelegramId(Long telegramId) { this.telegramId = telegramId; }

    public OffsetDateTime getDateRegistered() { return dateRegistered; }
    public void setDateRegistered(OffsetDateTime dateRegistered) { this.dateRegistered = dateRegistered; }

    @Override
    public String toString() {
        return "User{" +
               "fullName='" + fullName + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", email='" + email + '\'' +
               ", telegramId=" + telegramId +
               ", dateRegistered=" + dateRegistered +
               '}';
    }
}