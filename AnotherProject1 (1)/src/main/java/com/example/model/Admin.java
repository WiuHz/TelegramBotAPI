package com.example.model;

public class Admin {
    private Long telegramId;
    private String role; // admin

    public Long getTelegramId() { return telegramId; }
    public void setTelegramId(Long telegramId) { this.telegramId = telegramId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}