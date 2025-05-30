package com.example.model;

// Các bước bot hỏi khi User xài /register
public enum UserRegistrationStep {
    ASK_NAME,
    ASK_PHONE,
    ASK_EMAIL,
    CONFIRM,
    EDIT_NAME,
    EDIT_PHONE,
    EDIT_EMAIL,
    ASK_EVENT_ID,
    ASK_EVENT_NAME,
    ASK_EVENT_TIME,
    ASK_EVENT_DESCRIPTION,
    ASK_EVENT_GROUP_LINK,
    ASK_EVENT_CAPACITY,
    EDIT_EVENT_ID,
    EDIT_EVENT_NAME,
    EDIT_EVENT_TIME,
    EDIT_EVENT_DESCRIPTION,
    EDIT_EVENT_GROUP_LINK,
    EDIT_EVENT_CAPACITY
}