package com.example.service;

import com.example.airtable.AirtableClient;
import com.example.model.Event;
import com.example.model.Registration;
import com.example.model.User;
import com.example.util.Constant;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirtableService {
    private final AirtableClient airtableClient;

    public AirtableService() {
        this.airtableClient = new AirtableClient();
    }

    // Save or update a user in the User table in Airtable
    public boolean updateOrSaveUser(User user) {
        try {
            String telegramId = user.getTelegramId().toString();
            String filterFormula = "?filterByFormula={TelegramId}='" + telegramId + "'";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_USER + filterFormula);
            JSONObject fields = new JSONObject();
            fields.put("FullName", user.getFullName());
            fields.put("Phone", user.getPhoneNumber());
            fields.put("Email", user.getEmail());
            fields.put("TelegramId", telegramId);
            fields.put("DateRegistered", user.getDateRegistered().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            if (response != null) {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray records = jsonResponse.getJSONArray("records");
                if (records.length() > 0) {
                    String recordId = records.getJSONObject(0).getString("id");
                    JSONObject record = new JSONObject();
                    record.put("id", recordId);
                    record.put("fields", fields);

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("records", new JSONArray().put(record));

                    String patchResponse = airtableClient.patchToTable(Constant.AIRTABLE_API_USER, requestBody.toString());
                    if (patchResponse != null) {
                        System.out.println("User updated: " + user + ", Response: " + patchResponse);
                        return true;
                    } else {
                        System.out.println("Failed to update user: " + user + ", No response from Airtable");
                        return false;
                    }
                }
            }

            JSONObject record = new JSONObject();
            record.put("fields", fields);

            JSONObject requestBody = new JSONObject();
            requestBody.put("records", new JSONArray().put(record));

            String postResponse = airtableClient.postToTable(Constant.AIRTABLE_API_USER, requestBody.toString());
            if (postResponse != null) {
                System.out.println("User saved: " + user + ", Response: " + postResponse);
                return true;
            } else {
                System.out.println("Failed to save user: " + user + ", No response from Airtable");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error saving or updating user: " + user + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Save or update an event in the Event table in Airtable
    public boolean updateOrSaveEvent(Event event) {
        try {
            String eventId = event.getEventId();
            String filterFormula = "?filterByFormula={EventId}='" + eventId + "'";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_EVENT + filterFormula);
            JSONObject fields = new JSONObject();
            fields.put("EventId", event.getEventId());
            fields.put("EventName", event.getEventName());
            fields.put("Time", event.getTime());
            fields.put("Description", event.getDescription());
            fields.put("Group Link", event.getGroupLink());
            fields.put("Capacity", event.getCapacity());

            if (response != null) {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray records = jsonResponse.getJSONArray("records");
                if (records.length() > 0) {
                    String recordId = records.getJSONObject(0).getString("id");
                    JSONObject record = new JSONObject();
                    record.put("id", recordId);
                    record.put("fields", fields);

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("records", new JSONArray().put(record));

                    String patchResponse = airtableClient.patchToTable(Constant.AIRTABLE_API_EVENT, requestBody.toString());
                    if (patchResponse != null) {
                        System.out.println("Event updated: " + event.getEventId() + ", Response: " + patchResponse);
                        return true;
                    } else {
                        System.out.println("Failed to update event: " + event.getEventId() + ", No response from Airtable");
                        return false;
                    }
                }
            }

            JSONObject record = new JSONObject();
            record.put("fields", fields);

            JSONObject requestBody = new JSONObject();
            requestBody.put("records", new JSONArray().put(record));

            String postResponse = airtableClient.postToTable(Constant.AIRTABLE_API_EVENT, requestBody.toString());
            if (postResponse != null) {
                System.out.println("Event saved: " + event.getEventId() + ", Response: " + postResponse);
                return true;
            } else {
                System.out.println("Failed to save event: " + event.getEventId() + ", No response from Airtable");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error saving or updating event: " + event.getEventId() + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Update an event in Airtable using recordId
    public boolean updateEventWithRecordId(Event event, String recordId) {
        try {
            if (recordId == null) {
                System.out.println("Record ID is null, cannot update event: " + event.getEventId());
                return false;
            }

            JSONObject fields = new JSONObject();
            fields.put("EventId", event.getEventId());
            fields.put("EventName", event.getEventName());
            fields.put("Time", event.getTime());
            fields.put("Description", event.getDescription());
            fields.put("Group Link", event.getGroupLink());
            fields.put("Capacity", event.getCapacity());

            JSONObject record = new JSONObject();
            record.put("id", recordId);
            record.put("fields", fields);

            JSONObject requestBody = new JSONObject();
            requestBody.put("records", new JSONArray().put(record));

            String patchResponse = airtableClient.patchToTable(Constant.AIRTABLE_API_EVENT, requestBody.toString());
            if (patchResponse != null) {
                System.out.println("Event updated with recordId: " + recordId + ", Response: " + patchResponse);
                return true;
            } else {
                System.out.println("Failed to update event with recordId: " + recordId + ", No response from Airtable");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error updating event with recordId: " + recordId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Delete an event from the Event table in Airtable
    public boolean deleteEvent(String eventId) {
        try {
            String filterFormula = "?filterByFormula={EventId}='" + eventId + "'";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_EVENT + filterFormula);
            if (response == null) {
                System.out.println("No response when retrieving event for deletion: " + eventId);
                return false;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            if (records.length() == 0) {
                System.out.println("Event not found for Event ID: " + eventId);
                return false;
            }

            String recordId = records.getJSONObject(0).getString("id");
            boolean deleted = airtableClient.deleteFromTable(Constant.AIRTABLE_API_EVENT, recordId);
            if (deleted) {
                System.out.println("Event deleted: " + eventId);
            } else {
                System.out.println("Failed to delete event: " + eventId);
            }
            return deleted;
        } catch (Exception e) {
            System.err.println("Error deleting event: " + eventId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Delete all registrations associated with a specific event from the Registration table
    public boolean deleteRegistrationsByEventId(String eventId) {
        try {
            String filterFormula = "?filterByFormula={EventId}='" + eventId + "'";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_REGISTRATION + filterFormula);
            if (response == null) {
                System.out.println("No response when retrieving registrations for event: " + eventId);
                return false;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            if (records.length() == 0) {
                System.out.println("No registrations found for event: " + eventId);
                return true; // No registrations to delete, so consider it a success
            }

            boolean allDeleted = true;
            for (int i = 0; i < records.length(); i++) {
                String recordId = records.getJSONObject(i).getString("id");
                boolean deleted = airtableClient.deleteFromTable(Constant.AIRTABLE_API_REGISTRATION, recordId);
                if (!deleted) {
                    System.out.println("Failed to delete registration record: " + recordId + " for event: " + eventId);
                    allDeleted = false;
                } else {
                    System.out.println("Deleted registration record: " + recordId + " for event: " + eventId);
                }
            }
            return allDeleted;
        } catch (Exception e) {
            System.err.println("Error deleting registrations for event: " + eventId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve an event by Event ID
    public Event getEventById(String eventId) {
        try {
            String filterFormula = "?filterByFormula={EventId}='" + eventId + "'";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_EVENT + filterFormula);
            if (response == null) {
                System.out.println("No response when retrieving event: " + eventId);
                return null;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            if (records.length() == 0) {
                System.out.println("No event found for Event ID: " + eventId);
                return null;
            }

            JSONObject record = records.getJSONObject(0);
            JSONObject fields = record.getJSONObject("fields");

            Event event = new Event();
            event.setEventId(fields.getString("EventId"));
            event.setEventName(fields.getString("EventName"));
            event.setTime(fields.getString("Time"));
            event.setDescription(fields.getString("Description"));
            event.setGroupLink(fields.getString("Group Link"));
            event.setCapacity(fields.getInt("Capacity"));
            System.out.println("Retrieved event: " + eventId);
            return event;
        } catch (Exception e) {
            System.err.println("Error retrieving event: " + eventId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Retrieve an event along with its Airtable record ID
    public Map<String, Object> getEventWithRecordId(String eventId) {
        try {
            String filterFormula = "?filterByFormula={EventId}='" + eventId + "'";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_EVENT + filterFormula);
            if (response == null) {
                System.out.println("No response when retrieving event: " + eventId);
                return null;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            if (records.length() == 0) {
                System.out.println("No event found for Event ID: " + eventId);
                return null;
            }

            JSONObject record = records.getJSONObject(0);
            String recordId = record.getString("id");
            JSONObject fields = record.getJSONObject("fields");

            Event event = new Event();
            event.setEventId(fields.getString("EventId"));
            event.setEventName(fields.getString("EventName"));
            event.setTime(fields.getString("Time"));
            event.setDescription(fields.getString("Description"));
            event.setGroupLink(fields.getString("Group Link"));
            event.setCapacity(fields.getInt("Capacity"));

            Map<String, Object> result = new HashMap<>();
            result.put("event", event);
            result.put("recordId", recordId);
            System.out.println("Retrieved event with recordId: " + eventId);
            return result;
        } catch (Exception e) {
            System.err.println("Error retrieving event with recordId: " + eventId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Save a user to the User table in Airtable (kept for compatibility)
    public boolean saveUser(User user) {
        return updateOrSaveUser(user);
    }

    // Retrieve a user by Telegram ID
    public User getUserByTelegramId(String telegramId) {
        try {
            String filterFormula = "?filterByFormula={TelegramId}='" + telegramId + "'";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_USER + filterFormula);
            if (response == null) {
                System.out.println("No response when retrieving user: " + telegramId);
                return null;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            if (records.length() == 0) {
                System.out.println("No user found for Telegram ID: " + telegramId);
                return null;
            }

            JSONObject record = records.getJSONObject(0);
            JSONObject fields = record.getJSONObject("fields");

            User user = new User();
            user.setFullName(fields.getString("FullName"));
            user.setPhoneNumber(fields.getString("Phone"));
            user.setEmail(fields.getString("Email"));
            user.setTelegramId(Long.parseLong(fields.getString("TelegramId")));
            user.setDateRegistered(OffsetDateTime.parse(fields.getString("DateRegistered")));
            System.out.println("Retrieved user: " + user);
            return user;
        } catch (Exception e) {
            System.err.println("Error retrieving user: " + telegramId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Save a registration to the Registration table in Airtable
    public boolean saveRegistration(Registration registration) {
        try {
            JSONObject fields = new JSONObject();
            fields.put("UserId", registration.getUserId());
            fields.put("EventId", registration.getEventId());
            fields.put("Status", registration.getStatus());
            fields.put("RegisteredTime", registration.getRegisteredTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            JSONObject record = new JSONObject();
            record.put("fields", fields);

            JSONObject requestBody = new JSONObject();
            requestBody.put("records", new JSONArray().put(record));

            System.out.println("Sending registration to Airtable: " + requestBody.toString());
            String response = airtableClient.postToTable(Constant.AIRTABLE_API_REGISTRATION, requestBody.toString());
            System.out.println("the response is" + response);
            if (response != null) {
                System.out.println("Registration saved for user: " + registration.getUserId() + 
                                   ", event: " + registration.getEventId() + 
                                   ", Response: " + response);
                return true;
            } else {
                System.out.println("Failed to save registration for user: " + registration.getUserId() + 
                                   ", event: " + registration.getEventId() + 
                                   ", No response from Airtable");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error saving registration for user: " + registration.getUserId() + 
                               ", event: " + registration.getEventId() + 
                               ", Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getUpcomingEvents() {
        try {
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_EVENT + "?sort[0][field]=EventId&sort[0][direction]=asc");
            if (response == null) {
                System.out.println("No events found in Event table");
                return "Không thể lấy danh sách sự kiện.";
            }

            OffsetDateTime now = OffsetDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            StringBuilder eventList = new StringBuilder("Danh sách sự kiện sắp diễn ra:\n");

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                JSONObject fields = record.getJSONObject("fields");
                String time = fields.getString("Time");
                try {
                    LocalDateTime localEventTime = LocalDateTime.parse(time, formatter);
                    // Assume events use the same timezone as the server (e.g., UTC)
                    OffsetDateTime eventTime = localEventTime.atOffset(ZoneOffset.UTC); // Adjust to your timezone if needed
                    if (eventTime.isAfter(now)) {
                        String eventId = fields.getString("EventId");
                        String eventName = fields.getString("EventName");
                        String description = fields.getString("Description");
                        String groupLink = fields.getString("Group Link");
                        Integer capacity = fields.getInt("Capacity");
                        eventList.append("Event ID: ").append(eventId).append("\n")
                                .append("Event Name: ").append(eventName).append("\n")
                                .append("Time: ").append(time).append("\n")
                                .append("Description: ").append(description).append("\n")
                                .append("Group link: ").append(groupLink).append("\n")
                                .append("Capacity: ").append(capacity).append("\n")
                                .append("_________________________________________\n");
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Invalid time format for event: " + fields.getString("EventId") + ", Time: " + time);
                    continue; // Skip events with invalid time format
                }
            }

            return eventList.length() > "Danh sách sự kiện sắp diễn ra:\n".length() ? eventList.toString() : "Không có sự kiện sắp diễn ra.";
        } catch (Exception e) {
            System.err.println("Error retrieving upcoming events: " + e.getMessage());
            e.printStackTrace();
            return "Lỗi khi lấy danh sách sự kiện sắp diễn ra: " + e.getMessage();
        }
    }

    public List<Event> getUpcomingEventsList() {
        try {
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_EVENT + "?sort[0][field]=EventId&sort[0][direction]=asc");
            if (response == null) {
                System.out.println("No events found in Event table");
                return null; 
            }

            OffsetDateTime now = OffsetDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            List<Event> eventList = new ArrayList<>();

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                JSONObject fields = record.getJSONObject("fields");
                String time = fields.getString("Time");
                try {
                    LocalDateTime localEventTime = LocalDateTime.parse(time, formatter);
                    OffsetDateTime eventTime = localEventTime.atOffset(ZoneOffset.UTC); 
                    if (eventTime.isAfter(now)) {
                        Event event = new Event();
                        event.setEventId(fields.getString("EventId"));
                        event.setEventName(fields.getString("EventName"));
                        event.setTime(time);
                        event.setDescription(fields.getString("Description"));
                        event.setGroupLink(fields.getString("Group Link"));
                        event.setCapacity(fields.getInt("Capacity"));
                        eventList.add(event);
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Invalid time format for event: " + fields.getString("EventId") + ", Time: " + time);
                    continue; // 
                }
            }

            return eventList.isEmpty() ? null : eventList; 
        } catch (Exception e) {
            System.err.println("Error retrieving upcoming events: " + e.getMessage());
            e.printStackTrace();
            return null; 
        }
    }

    // Retrieve all events from the Event table
    public String getAllEvents() {
        try {
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_EVENT + "?sort[0][field]=EventId&sort[0][direction]=asc");
            if (response == null) {
                System.out.println("No events found in Event table");
                return "Không thể lấy danh sách sự kiện.";
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            StringBuilder eventList = new StringBuilder("Danh sách sự kiện:\n");

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                JSONObject fields = record.getJSONObject("fields");
                String eventId = fields.getString("EventId");
                String eventName = fields.getString("EventName");
                String time = fields.getString("Time");
                String description = fields.getString("Description");
                String groupLink = fields.getString("Group Link");
                Integer capacity = fields.getInt("Capacity");
                eventList.append("Event ID: ").append(eventId).append("\n")
                        .append("Event Name: ").append(eventName).append("\n")
                        .append("Time: ").append(time).append("\n")
                        .append("Description: ").append(description).append("\n")
                        .append("Group link: ").append(groupLink).append("\n")
                        .append("Capacity: ").append(capacity).append("\n")
                        .append("_________________________________________\n");
            }

            return eventList.length() > "Danh sách sự kiện:\n".length() ? eventList.toString() : "Không có sự kiện nào.";
        } catch (Exception e) {
            System.err.println("Error retrieving events: " + e.getMessage());
            e.printStackTrace();
            return "Lỗi khi lấy danh sách sự kiện: " + e.getMessage();
        }
    }

    public List<Event> getAllEventsList(){
        List<Event> events = new ArrayList<>();
        try {
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_EVENT + "?sort[0][field]=EventId&sort[0][direction]=asc");
            if (response == null) {
                System.out.println("No events found in Event table");
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            StringBuilder eventList = new StringBuilder("Danh sách sự kiện:\n");

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                JSONObject fields = record.getJSONObject("fields");
            
                Event event = new Event();
                event.setEventId(fields.optString("Event ID"));
                event.setEventName(fields.optString("Event Name"));
                event.setTime(fields.optString("Time"));
                event.setDescription(fields.optString("Description"));
                event.setGroupLink(fields.optString("Group Link"));
                event.setCapacity(fields.optInt("Capacity"));
                event.setEventStatus(fields.optString("Event Status"));

                events.add(event);
            }

            
        } catch (Exception e) {
            System.err.println("Error retrieving events: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Lỗi khi lấy danh sách sự kiện: " + e.getMessage());
        }
        return events;
    }

    // Retrieve all registrations from the Registration table
    public String getAllRegistrations() {
        try {
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_REGISTRATION);
            if (response == null) {
                System.out.println("No registrations found in Registration table");
                return "Không thể lấy danh sách đăng ký.";
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            StringBuilder registrationList = new StringBuilder("Danh sách đăng ký:\n");

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                JSONObject fields = record.getJSONObject("fields");
                String userId = fields.getString("UserId");
                String eventId = fields.getString("EventId");
                String status = fields.getString("Status");
                registrationList.append("User: ").append(userId)
                        .append(" - Sự kiện: ").append(eventId)
                        .append(": ").append(status).append("\n");
            }

            return registrationList.length() > "Danh sách đăng ký:\n".length() ? registrationList.toString() : "Không có đăng ký nào.";
        } catch (Exception e) {
            System.err.println("Error retrieving registrations: " + e.getMessage());
            e.printStackTrace();
            return "Lỗi khi lấy danh sách đăng ký: " + e.getMessage();
        }
    }

    public List<Registration> getAllRegistrationsList(){
        List<Registration> registrations = new ArrayList<>();
        try{
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_REGISTRATION);
            if(response == null){
                System.out.println("Không có ai trong danh sách đăng ký!");
                return registrations;
            }
            
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                JSONObject fields = record.getJSONObject("fields");


            Registration registration = new Registration();
            registration.setUserId(fields.optString("UserId", "Vui lòng cung cấp thêm thông tin"));
            registration.setEventId(fields.optString("EventId", "Vui lòng cung cấp thêm thông tin"));
            registration.setStatus(fields.optString("Status", "Vui lòng cung cấp thêm thông tin"));
            registration.setRegisteredTime(OffsetDateTime.now());

            registrations.add(registration);
        } 
        }catch(Exception e){
            System.err.println("Error retrieving registrations: " + e.getMessage());
            e.printStackTrace();
        }
        return registrations;
    }

    // Retrieve registrations for a specific user
    public String getUserRegistrations(String userId) {
        try {
            String filterFormula = "?filterByFormula={UserId}='" + userId + "'";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_REGISTRATION + filterFormula);
            if (response == null) {
                System.out.println("No registrations found for user: " + userId);
                return "Bạn chưa đăng ký sự kiện nào.";
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            if (records.length() == 0) {
                System.out.println("No registrations found for user: " + userId);
                return "Bạn chưa đăng ký sự kiện nào.";
            }

            String eventResponse = airtableClient.getFromTable(Constant.AIRTABLE_API_EVENT);
            if (eventResponse == null) {
                System.out.println("No events found in Event table");
                return "Không thể lấy thông tin sự kiện.";
            }

            JSONObject eventJsonResponse = new JSONObject(eventResponse);
            JSONArray eventRecords = eventJsonResponse.getJSONArray("records");
            Map<String, JSONObject> eventMap = new HashMap<>();
            for (int i = 0; i < eventRecords.length(); i++) {
                JSONObject eventRecord = eventRecords.getJSONObject(i);
                JSONObject fields = eventRecord.getJSONObject("fields");
                String eventId = fields.getString("EventId");
                eventMap.put(eventId, fields);
            }

            StringBuilder registrationList = new StringBuilder("Các sự kiện bạn đã đăng ký:\n");
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                JSONObject fields = record.getJSONObject("fields");
                String eventId = fields.getString("EventId");
                String status = fields.getString("Status");

                JSONObject eventFields = eventMap.get(eventId);
                String eventInfo = eventFields != null 
                    ? eventFields.getString("EventName") + " (" + eventFields.getString("Time") + ")"
                    : eventId;

                registrationList.append(i + 1).append(". ")
                                .append(eventInfo)
                                .append(" (Trạng thái: ").append(status).append(")\n");
            }

            return registrationList.toString();
        } catch (Exception e) {
            System.err.println("Error retrieving registrations for user: " + userId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return "Lỗi khi lấy danh sách sự kiện đã đăng ký: " + e.getMessage();
        }
    }

    // Update the status of a registration
    public void updateRegistrationStatus(String userId, String eventId, String status) {
        try {
            String filterFormula = "?filterByFormula=AND({UserId}='" + userId + "',{EventId}='" + eventId + "')";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_REGISTRATION + filterFormula);
            if (response == null) {
                System.out.println("Registration not found for user: " + userId + ", event: " + eventId);
                return;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            if (records.length() == 0) {
                System.out.println("Registration not found for user: " + userId + ", event: " + eventId);
                return;
            }

            String recordId = records.getJSONObject(0).getString("id");

            JSONObject fields = new JSONObject();
            fields.put("Status", status);

            JSONObject record = new JSONObject();
            record.put("id", recordId);
            record.put("fields", fields);

            JSONObject requestBody = new JSONObject();
            requestBody.put("records", new JSONArray().put(record));

            String patchResponse = airtableClient.patchToTable(Constant.AIRTABLE_API_REGISTRATION, requestBody.toString());
            if (patchResponse != null) {
                System.out.println("Updated registration status for user: " + userId + ", event: " + eventId + " to: " + status);
            } else {
                System.out.println("Failed to update registration status for user: " + userId + ", event: " + eventId);
            }
        } catch (Exception e) {
            System.err.println("Error updating registration status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Map<String, String>> approveAllRegistrationsForEvent(String eventId) {
        try {
            // Get event details to check capacity
            Event event = getEventById(eventId);
            if (event == null) {
                System.out.println("Event not found: " + eventId);
                return new ArrayList<>();
            }
            int capacity = event.getCapacity();

            // Count already accepted registrations
            String acceptedFilter = "?filterByFormula=AND({EventId}='" + eventId + "',{Status}='Accepted')";
            String acceptedResponse = airtableClient.getFromTable(Constant.AIRTABLE_API_REGISTRATION + acceptedFilter);
            int acceptedCount = 0;
            if (acceptedResponse != null) {
                JSONObject acceptedJson = new JSONObject(acceptedResponse);
                acceptedCount = acceptedJson.getJSONArray("records").length();
            }

            // Get pending registrations, sorted by RegisteredTime
            String filterFormula = "?filterByFormula=AND({EventId}='" + eventId + "',{Status}='Pending')&sort[0][field]=RegisteredTime&sort[0][direction]=asc";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_REGISTRATION + filterFormula);
            if (response == null) {
                System.out.println("No response when retrieving pending registrations for event: " + eventId);
                return new ArrayList<>();
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            if (records.length() == 0) {
                System.out.println("No pending registrations found for event: " + eventId);
                return new ArrayList<>();
            }

            // Calculate how many more users can be approved
            int remainingCapacity = Math.max(0, capacity - acceptedCount);
            int usersToApprove = Math.min(records.length(), remainingCapacity);
            if (usersToApprove == 0) {
                System.out.println("No remaining capacity for event: " + eventId + " (Capacity: " + capacity + ", Accepted: " + acceptedCount + ")");
                return new ArrayList<>();
            }

            List<Map<String, String>> approvedUsers = new ArrayList<>();
            JSONArray updateRecords = new JSONArray();
            for (int i = 0; i < usersToApprove; i++) {
                JSONObject record = records.getJSONObject(i);
                String recordId = record.getString("id");
                JSONObject fields = record.getJSONObject("fields");
                String userId = fields.getString("UserId");

                JSONObject updateFields = new JSONObject();
                updateFields.put("Status", "Accepted");
                JSONObject updateRecord = new JSONObject();
                updateRecord.put("id", recordId);
                updateRecord.put("fields", updateFields);
                updateRecords.put(updateRecord);

                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("userId", userId);
                approvedUsers.add(userInfo);
            }

            if (!updateRecords.isEmpty()) {
                JSONObject requestBody = new JSONObject();
                requestBody.put("records", updateRecords);
                String patchResponse = airtableClient.patchToTable(Constant.AIRTABLE_API_REGISTRATION, requestBody.toString());
                if (patchResponse != null) {
                    System.out.println("Approved " + approvedUsers.size() + " registrations for event: " + eventId);
                    return approvedUsers;
                } else {
                    System.out.println("Failed to update registrations for event: " + eventId);
                    return new ArrayList<>();
                }
            } else {
                System.out.println("No registrations to update for event: " + eventId);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error approving registrations for event: " + eventId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<String> getRegisteredUserIdsForEvent(String eventId) {
        try {
            String filterFormula = "?filterByFormula={EventId}='" + eventId + "'";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_REGISTRATION + filterFormula);
            if (response == null) {
                System.out.println("No response when retrieving registrations for event: " + eventId);
                return new ArrayList<>();
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            List<String> userIds = new ArrayList<>();
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                JSONObject fields = record.getJSONObject("fields");
                String userId = fields.getString("UserId");
                userIds.add(userId);
            }
            System.out.println("Retrieved " + userIds.size() + " registered user IDs for event: " + eventId);
            return userIds;
        } catch (Exception e) {
            System.err.println("Error retrieving registered user IDs for event: " + eventId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Check if an event exists in the Event table
    public boolean eventExists(String eventId) {
        try {
            String filterFormula = "?filterByFormula={EventId}='" + eventId + "'";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_EVENT + filterFormula);
            if (response == null) {
                System.out.println("No response when checking event: " + eventId);
                return false;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            return records.length() > 0;
        } catch (Exception e) {
            System.err.println("Error checking event existence: " + eventId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Check if a user is registered for a specific event
    public boolean isUserRegistered(String userId, String eventId) {
        try {
            String filterFormula = "?filterByFormula=AND({UserId}='" + userId + "',{EventId}='" + eventId + "')";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_REGISTRATION + filterFormula);
            if (response == null) {
                System.out.println("No response when checking registration for user: " + userId + ", event: " + eventId);
                return false;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            return records.length() > 0;
        } catch (Exception e) {
            System.err.println("Error checking registration for user: " + userId + ", event: " + eventId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Cancel a registration by deleting it from the Registration table
    public boolean cancelRegistration(String userId, String eventId) {
        try {
            String filterFormula = "?filterByFormula=AND({UserId}='" + userId + "',{EventId}='" + eventId + "')";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_REGISTRATION + filterFormula);
            if (response == null) {
                System.out.println("No response when checking registration for cancellation: user: " + userId + ", event: " + eventId);
                return false;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            if (records.length() == 0) {
                System.out.println("Registration not found for user: " + userId + ", event: " + eventId);
                return false;
            }

            String recordId = records.getJSONObject(0).getString("id");
            boolean deleted = airtableClient.deleteFromTable(Constant.AIRTABLE_API_REGISTRATION, recordId);
            if (deleted) {
                System.out.println("Registration deleted for user: " + userId + ", event: " + eventId);
            } else {
                System.out.println("Failed to delete registration for user: " + userId + ", event: " + eventId);
            }
            return deleted;
        } catch (Exception e) {
            System.err.println("Error cancelling registration for user: " + userId + ", event: " + eventId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public User getUserById(String userId) {
        try {
            String filterFormula = "?filterByFormula={UserId}='" + userId + "'";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_USER + filterFormula);
            if (response == null) {
                System.out.println("No response when retrieving user: " + userId);
                return null;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            if (records.length() == 0) {
                System.out.println("No user found for User ID: " + userId);
                return null;
            }

            JSONObject record = records.getJSONObject(0);
            JSONObject fields = record.getJSONObject("fields");

            User user = new User();
            user.setFullName(fields.getString("FullName"));
            user.setPhoneNumber(fields.getString("Phone"));
            user.setEmail(fields.getString("Email"));
            user.setTelegramId(Long.parseLong(fields.getString("TelegramId")));
            user.setDateRegistered(OffsetDateTime.parse(fields.getString("DateRegistered")));
            System.out.println("Retrieved user: " + user);
            return user;
        } catch (Exception e) {
            System.err.println("Error retrieving user: " + userId + ", Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateRegistrationsEventId(String oldEventId, String newEventId) {
        try {
            String filterFormula = "?filterByFormula={EventId}='" + oldEventId + "'";
            String response = airtableClient.getFromTable(Constant.AIRTABLE_API_REGISTRATION + filterFormula);
            if (response == null) {
                System.out.println("No response when retrieving registrations for event: " + oldEventId);
                return false;
            }

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONArray("records");
            if (records.length() == 0) {
                System.out.println("No registrations found for event: " + oldEventId);
                return true; // No registrations to update, consider success
            }

            JSONArray updateRecords = new JSONArray();
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                String recordId = record.getString("id");
                JSONObject fields = new JSONObject();
                fields.put("EventId", newEventId);
                JSONObject updateRecord = new JSONObject();
                updateRecord.put("id", recordId);
                updateRecord.put("fields", fields);
                updateRecords.put(updateRecord);
            }

            JSONObject requestBody = new JSONObject();
            requestBody.put("records", updateRecords);

            String patchResponse = airtableClient.patchToTable(Constant.AIRTABLE_API_REGISTRATION, requestBody.toString());
            if (patchResponse != null) {
                System.out.println("Updated EventId from " + oldEventId + " to " + newEventId + " in " + records.length() + " registrations");
                return true;
            } else {
                System.out.println("Failed to update registrations for EventId from " + oldEventId + " to " + newEventId);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error updating registrations for EventId from " + oldEventId + " to " + newEventId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}