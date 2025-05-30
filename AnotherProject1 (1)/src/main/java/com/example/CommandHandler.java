package com.example;

import com.example.model.*;
import com.example.service.AirtableService;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CommandHandler {
    private final AirtableService airtableService = new AirtableService();
    private final Map<Long, UserRegistrationState> userStates = new HashMap<>();
    private final Set<Long> adminIds = new HashSet<>();

    public CommandHandler() {
        adminIds.add(7451061215L);
    }

    public void handleMessage(Message message, MyBot bot) {
        Long userId = message.getFrom().getId();
        String text = message.getText().trim();
        Long chatId = message.getChatId();

    if (userStates.containsKey(userId)) {
            UserRegistrationState state = userStates.get(userId);
            UserRegistrationStep step = state.getStep();

            // Handle registration steps (ASK_NAME, ASK_PHONE, ASK_EMAIL)
            if (step == UserRegistrationStep.ASK_NAME || step == UserRegistrationStep.ASK_PHONE ||
                step == UserRegistrationStep.ASK_EMAIL) {
                if (text.equals("/registration_cancel")) {
                    userStates.remove(userId);
                    send(bot, chatId, "Bạn đã hủy quá trình đăng ký.");
                    return;
                } else if (text.startsWith("/")) {
                    send(bot, chatId, "Bạn đang trong quá trình đăng ký. Vui lòng hoàn thành hoặc dùng /registration_cancel để hủy.");
                    return;
                }
            }
            // Handle event creation steps
            else if (step == UserRegistrationStep.ASK_EVENT_ID || step == UserRegistrationStep.ASK_EVENT_NAME ||
                    step == UserRegistrationStep.ASK_EVENT_TIME || step == UserRegistrationStep.ASK_EVENT_DESCRIPTION ||
                    step == UserRegistrationStep.ASK_EVENT_GROUP_LINK || step == UserRegistrationStep.ASK_EVENT_CAPACITY) {
                if (text.equals("/event_creation_cancel")) {
                    userStates.remove(userId);
                    send(bot, chatId, "Bạn đã hủy quá trình tạo sự kiện.");
                    return;
                } else if (text.startsWith("/")) {
                    send(bot, chatId, "Bạn đang trong quá trình tạo sự kiện. Vui lòng hoàn thành hoặc dùng /event_creation_cancel để hủy.");
                    return;
                }
            }
            // Handle CONFIRM step
            else if (step == UserRegistrationStep.CONFIRM) {
                if (text.equalsIgnoreCase("OK") ||
                    text.equals("/registration_cancel") || text.equals("/event_creation_cancel") ||
                    text.equals("/edit_name") || text.equals("/edit_phone") || text.equals("/edit_email") ||
                    text.equals("/edit_event_id") || text.equals("/edit_event_name") || text.equals("/edit_event_time") ||
                    text.equals("/edit_event_description") || text.equals("/edit_event_group_link") || text.equals("/edit_event_capacity")) {
                    // Allow these commands to be processed in the switch case below
                } else if (text.startsWith("/")) {
                    send(bot, chatId, "Bạn đang trong bước xác nhận. Vui lòng gửi 'OK' để xác nhận hoặc dùng các lệnh sửa đổi (/edit_) hoặc hủy (/registration_cancel, /event_creation_cancel).");
                    return;
                }
            }
        }

        // Admin commands
        if (adminIds.contains(userId)) {
            if (text.equals("/admin")) {
                send(bot, chatId, "Admin Granted");
            }
            if (text.startsWith("/admin_list_registrations")) {
                String registrations = airtableService.getAllRegistrations();
                User users = airtableService.getUserById(userId.toString());

                if(users == null){
                    System.out.println("Không thể tìm thấy người dùng với ID: " + userId);
                    send(bot, chatId, "Không thể tìm thấy thông tin của người dùng với ID: " + userId);
                    return;
                }

                String fullName = users.getFullName();
                String phoneNumber = users.getPhoneNumber();
                String email = users.getEmail();
                Long telegramId = users.getTelegramId();
                OffsetDateTime dateRegistered = users.getDateRegistered();

                send(bot, chatId, "Thông tin của người dùng đăng kí sự kiện bao gồm ID: " + userId + ", Họ Tên: " + fullName + ", SĐT: " + phoneNumber + ", email: " + ", Telegram ID: " + telegramId + ", dateRegistered: " + dateRegistered);
                return;
            }
            if (text.startsWith("/approve ")) {
                String[] parts = text.split("\\s+");
                if (parts.length < 2) {
                    send(bot, chatId, "Vui lòng cung cấp ít nhất một Event ID. Ví dụ: /approve 1 2 3");
                    return;
                }
                List<String> eventIds = Arrays.asList(parts).subList(1, parts.length);
                List<String> invalidEventIds = new ArrayList<>();
                Map<String, List<Map<String, String>>> approvedUsersByEvent = new HashMap<>();
                
                for (String eventId : eventIds) {
                    if (!airtableService.eventExists(eventId)) {
                        invalidEventIds.add(eventId);
                    } else {
                        List<Map<String, String>> approvedUsers = airtableService.approveAllRegistrationsForEvent(eventId);
                        if (!approvedUsers.isEmpty()) {
                            approvedUsersByEvent.put(eventId, approvedUsers);
                        }
                    }
                }

                if (!invalidEventIds.isEmpty()) {
                    send(bot, chatId, "Các Event ID không hợp lệ: " + String.join(", ", invalidEventIds) + ". Vui lòng kiểm tra lại.");
                    if (approvedUsersByEvent.isEmpty()) {
                        return;
                    }
                }

                StringBuilder response = new StringBuilder();
            for (Map.Entry<String, List<Map<String, String>>> entry : approvedUsersByEvent.entrySet()) {
                String eventId = entry.getKey();
                List<Map<String, String>> approvedUsers = entry.getValue();
                int successfulNotifications = 0;
                for (Map<String, String> user : approvedUsers) {
                    String approved_userId = user.get("userId");
                    try {
                        send(bot, Long.parseLong(approved_userId), "Đăng ký của bạn cho sự kiện " + eventId + " đã được duyệt!");
                        successfulNotifications++;
                    } catch (Exception e) {
                        System.err.println("Failed to send notification to user " + userId + " for event " + eventId + ": " + e.getMessage());
                    }
                }
                response.append("Sự kiện ").append(eventId).append(": Đã duyệt ").append(approvedUsers.size()).append(" người dùng, thông báo thành công ").append(successfulNotifications).append("/").append(approvedUsers.size()).append(".\n");
            }

                if (response.length() == 0) {
                    send(bot, chatId, "Không có người dùng nào ở trạng thái Pending cho các sự kiện được chỉ định.");
                } else {
                    send(bot, chatId, response.toString());
                }
                return;
            }
            if (text.startsWith("/create_event")) {
                if (!text.equals("/create_event")) {
                    send(bot, chatId, "Lệnh không hợp lệ. Sử dụng /create_event để tạo sự kiện.");
                    return;
                }
                UserRegistrationState state = new UserRegistrationState();
                state.setEvent(new Event());
                userStates.put(userId, state);
                state.setStep(UserRegistrationStep.ASK_EVENT_ID);
                send(bot, chatId, "Nhập Event ID:");
                return;
            }

            if (text.startsWith("/close_events")){
                String[] parts = text.split("\\s+");
                if(parts.length != 2){
                    send(bot, chatId, "Vui lòng nhập id sự kiện cần đóng. Ví dụ: /close_events 1");
                    return;
                }
                String eventId = parts[1];
                Event existingEvent = airtableService.getEventById(eventId);
                if(existingEvent == null){
                    send(bot, chatId, "Sự kiện này không tồn tại!");
                    return;
                }

                existingEvent.setEventStatus("EXPIRED");
                boolean eventUpdated = airtableService.updateEventWithRecordId(existingEvent, eventId);
                if(eventUpdated){
                    send(bot, chatId, "Sự kiện " + existingEvent.getEventName() + " đã được đóng thành công.");
                }
                else{
                    send(bot, chatId, "Lỗi khi đóng sự kiện. Vui lòng kiểm tra lại.");
                }
                return;
            }

            if (text.startsWith("/edit_events")) {
                String[] parts = text.split("\\s+");
                if (parts.length != 2) {
                    send(bot, chatId, "Vui lòng cung cấp Event ID. Ví dụ: /edit_events 5");
                    return;
                }
                String eventId = parts[1];
                Map<String, Object> eventData = airtableService.getEventWithRecordId(eventId);
                if (eventData == null || eventData.get("event") == null) {
                    send(bot, chatId, "Sự kiện với ID " + eventId + " không tồn tại. Vui lòng kiểm tra lại.");
                    return;
                }
                Event existingEvent = (Event) eventData.get("event");
                String recordId = (String) eventData.get("recordId");
                UserRegistrationState state = new UserRegistrationState();
                state.setEvent(existingEvent);
                state.setEventIds(new ArrayList<>());
                state.setAirtableRecordId(recordId);
                if(existingEvent.getEventStatus().equals("EXPIRED")){
                    send(bot, chatId, "Sự kiện " + existingEvent.getEventName() + " đã hết hạn, không thể thực hiện thao tác!");
                    return;
                }
                // Store a copy of the original event for comparison
                Event originalEvent = new Event();
                originalEvent.setEventId(existingEvent.getEventId());
                originalEvent.setEventName(existingEvent.getEventName());
                originalEvent.setTime(existingEvent.getTime());
                originalEvent.setDescription(existingEvent.getDescription());
                originalEvent.setGroupLink(existingEvent.getGroupLink());
                originalEvent.setCapacity(existingEvent.getCapacity());
                state.setOriginalEvent(originalEvent);
                state.setStep(UserRegistrationStep.CONFIRM);
                userStates.put(userId, state);
                send(bot, chatId, formatEventSummary(existingEvent) +
                        "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận (nếu không chỉnh sửa), hoặc dùng các lệnh:\n" +
                        "/edit_event_id\n/edit_event_name\n/edit_event_time\n/edit_event_description\n/edit_event_group_link\n/edit_event_capacity để sửa.");
                return;
            }
            if (text.startsWith("/delete_event")) {
                String[] parts = text.split("\\s+");
                if (parts.length != 2) {
                    send(bot, chatId, "Vui lòng cung cấp Event ID. Ví dụ: /delete_event 5");
                    return;
                }
                String eventId = parts[1];
                Event existingEvent = airtableService.getEventById(eventId);
                if (existingEvent == null) {
                    send(bot, chatId, "Sự kiện với ID " + eventId + " không tồn tại. Vui lòng kiểm tra lại.");
                    return;
                }
                boolean registrationsDeleted = airtableService.deleteRegistrationsByEventId(eventId);
                if (!registrationsDeleted) {
                    send(bot, chatId, "Lỗi khi xóa các đăng ký liên quan đến sự kiện " + eventId + ". Vui lòng thử lại sau.");
                    return;
                }
                boolean eventDeleted = airtableService.deleteEvent(eventId);
                if (!eventDeleted) {
                    send(bot, chatId, "Lỗi khi xóa sự kiện " + eventId + ". Vui lòng thử lại sau.");
                    return;
                }
                if(existingEvent.getEventStatus().equals("EXPIRED")){
                    send(bot, chatId, "Sự kiện này " + existingEvent.getEventName() + " đã hết hạn, không thể thao tác!");
                }
                send(bot, chatId, "Đã xóa sự kiện " + eventId + " và tất cả các đăng ký liên quan thành công.");
                return;
            }
        }

        if (text.equals("/number_approved")){
            int count_approved = 0;
            List<Registration> registrations = airtableService.getAllRegistrationsList();
            for (Registration i : registrations){
                if(i.getStatus().equals("ACCEPTED")){
                    count_approved += 1;
                }
            }
            send(bot, chatId, "Tổng số người đăng kí là " + count_approved + " người!");
            return;
        }

        // User commands
        if (text.equals("/help")) {
            StringBuilder helpMessage = new StringBuilder("Các lệnh hỗ trợ:\n" +
                    "/register <event_id1> <event_id2> ... - Đăng ký một hoặc nhiều sự kiện\n" +
                    "/cancel <event_id1> <event_id2> ... - Hủy đăng ký một hoặc nhiều sự kiện\n" +
                    "/event - Xem danh sách sự kiện (người dùng thấy sự kiện sắp tới, admin thấy tất cả)\n" +
                    "/my_events - Xem danh sách sự kiện bạn đã đăng ký\n" +
                    "/edit_name - Chỉnh sửa tên họ\n" +
                    "/edit_phone - Chỉnh sửa số điện thoại\n" +
                    "/edit_email - Chỉnh sửa email\n" +
                    "/registration_cancel - Hủy quá trình đăng ký đang thực hiện\n");
            if (adminIds.contains(userId)) {
                helpMessage.append("\nLệnh dành cho Admin:\n" +
                        "/create_event - Tạo sự kiện mới\n" +
                        "/event_creation_cancel - Hủy quá trình tạo hoặc chỉnh sửa sự kiện\n" +
                        "/edit_events <event_id> - Chỉnh sửa sự kiện (thông báo sẽ được gửi đến người đăng ký nếu có thay đổi)\n" +
                        "/delete_event <event_id> - Xóa sự kiện và các đăng ký liên quan\n" +
                        "/admin_list_registrations - Xem danh sách tất cả đăng ký\n" +
                        "/admin_approve <user_id> <event_id> - Phê duyệt đăng ký của một người dùng cho một sự kiện\n" +
                        "/approve <event_id1> <event_id2> ... - Phê duyệt tất cả đăng ký đang chờ cho một hoặc nhiều sự kiện (tôn trọng sức chứa, ưu tiên người đăng ký sớm)\n");
            }
            send(bot, chatId, helpMessage.toString());
            return;
        }

        if (text.equals("/my_events")) {
            String registeredEvents = airtableService.getUserRegistrations(userId.toString());
            send(bot, chatId, registeredEvents);
            return;
        }

        if (text.startsWith("/register")) {
            String[] eventIds = text.replace("/register", "").trim().split("\\s+");
            if (eventIds.length == 0 || eventIds[0].isEmpty()) {
                send(bot, chatId, "Vui lòng cung cấp ít nhất một ID sự kiện. Ví dụ: /register 1 2 3");
                return;
            }

            List<String> validEventIds = new ArrayList<>();
            List<String> invalidEventIds = new ArrayList<>();
            for (String eventId : eventIds) {
                if (airtableService.eventExists(eventId)) {
                    validEventIds.add(eventId);
                } else {
                    invalidEventIds.add(eventId);
                }
            }

            if (!invalidEventIds.isEmpty()) {
                send(bot, chatId, "Các ID sự kiện không hợp lệ: " + String.join(", ", invalidEventIds) +
                        ". Vui lòng chọn lại. Danh sách sự kiện: /event");
                return;
            }

            List<String> alreadyRegistered = new ArrayList<>();
            List<String> newEventIds = new ArrayList<>();
            for (String eventId : validEventIds) {
                if (airtableService.isUserRegistered(userId.toString(), eventId)) {
                    alreadyRegistered.add(eventId);
                } else {
                    newEventIds.add(eventId);
                }
            }

            if (!alreadyRegistered.isEmpty()) {
                send(bot, chatId, "Bạn đã đăng ký các sự kiện: " + String.join(", ", alreadyRegistered) +
                        ". Vui lòng chọn các sự kiện khác.");
                if (newEventIds.isEmpty()) {
                    return;
                }
            }

            if (newEventIds.isEmpty()) {
                return;
            }

            User existingUser = airtableService.getUserByTelegramId(userId.toString());
            UserRegistrationState state = new UserRegistrationState();
            state.setEventIds(newEventIds);
            userStates.put(userId, state);


            if (existingUser != null) {
                state.setUser(existingUser);
                state.setStep(UserRegistrationStep.CONFIRM);
                send(bot, chatId, formatUserSummary(existingUser, state.getEventIds()) +
                        "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận, hoặc dùng các lệnh:\n/edit_name\n/edit_phone\n/edit_email để sửa.");
            } else {
                User newUser = new User();
                state.setUser(newUser);
                state.setStep(UserRegistrationStep.ASK_NAME);
                send(bot, chatId, "Họ tên của bạn là gì?");
            }
            return;
        }

        if (text.startsWith("/cancel")) {
            String[] eventIds = text.replace("/cancel", "").trim().split("\\s+");
            if (eventIds.length == 0 || eventIds[0].isEmpty()) {
                send(bot, chatId, "Vui lòng cung cấp ít nhất một ID sự kiện để hủy. Ví dụ: /cancel 1 2 3");
                return;
            }

            List<String> canceledEvents = new ArrayList<>();
            List<String> notRegisteredEvents = new ArrayList<>();
            for (String eventId : eventIds) {
                if (airtableService.isUserRegistered(userId.toString(), eventId)) {
                    airtableService.cancelRegistration(userId.toString(), eventId);
                    canceledEvents.add(eventId);
                } else {
                    notRegisteredEvents.add(eventId);
                }
            }

            StringBuilder response = new StringBuilder();
            if (!canceledEvents.isEmpty()) {
                response.append("Đã hủy đăng ký các sự kiện: ").append(String.join(", ", canceledEvents)).append("\n");
            }
            if (!notRegisteredEvents.isEmpty()) {
                response.append("Bạn chưa đăng ký các sự kiện: ").append(String.join(", ", notRegisteredEvents));
            }
            send(bot, chatId, response.toString());
            return;
        }

        if (text.equals("/event")) {
            List<Event> allEvents = airtableService.getAllEventsList(); 
            List<Event> upcomingEvents = airtableService.getUpcomingEventsList(); 

            StringBuilder pastEvents = new StringBuilder("The list of events was expired:\n");
            StringBuilder upcomingEventsList = new StringBuilder("The list of events upcoming:\n");

            for (Event event : allEvents) {
                if (event.getEventStatus().equals("EXPIRED")) {
                    pastEvents.append("Event ID: ").append(event.getEventId()).append("\n")
                            .append("Event Name: ").append(event.getEventName()).append("\n")
                            .append("Time: ").append(event.getTime()).append("\n")
                            .append("Description: ").append(event.getDescription()).append("\n")
                            .append("Group Link: ").append(event.getGroupLink()).append("\n")
                            .append("Capacity: ").append(event.getCapacity()).append("\n")
                            .append("_________________________________________\n");
                }
            }

            // Xử lý sự kiện sắp diễn ra
            for (Event event : upcomingEvents) {
                upcomingEventsList.append("Event ID: ").append(event.getEventId()).append("\n")
                                .append("Event Name: ").append(event.getEventName()).append("\n")
                                .append("Time: ").append(event.getTime()).append("\n")
                                .append("Description: ").append(event.getDescription()).append("\n")
                                .append("Group Link: ").append(event.getGroupLink()).append("\n")
                                .append("Capacity: ").append(event.getCapacity()).append("\n")
                                .append("Event Status: ").append(event.getEventStatus()).append("\n")
                                .append("_________________________________________\n");
            }

            send(bot, chatId, pastEvents.length() > "Danh sách các sự kiện đã diễn ra:\n".length() ? pastEvents.toString() : "Không có sự kiện đã diễn ra.");

            send(bot, chatId, upcomingEventsList.length() > "Danh sách các sự kiện sắp diễn ra:\n".length() ? upcomingEventsList.toString() : "Không có sự kiện sắp diễn ra.");
            
            return;
        }

        // Handle registration steps
        if (userStates.containsKey(userId)) {
            UserRegistrationState state = userStates.get(userId);
            User user = state.getUser();
            Event event = state.getEvent();

            switch (state.getStep()) {
                // User registration steps
                case ASK_NAME:
                    if (text.isEmpty()) {
                        send(bot, chatId, "Tên không được để trống. Vui lòng nhập lại:");
                        return;
                    }
                    user.setFullName(text);
                    state.setStep(UserRegistrationStep.ASK_PHONE);
                    send(bot, chatId, "Số điện thoại của bạn?");
                    break;
                case ASK_PHONE:
                    if (!isValidPhone(text)) {
                        send(bot, chatId, "Số điện thoại không hợp lệ (phải bắt đầu bằng +84 hoặc 0 và có 10 chữ số). Vui lòng nhập lại:");
                        return;
                    }
                    user.setPhoneNumber(text);
                    state.setStep(UserRegistrationStep.ASK_EMAIL);
                    send(bot, chatId, "Email của bạn?");
                    break;
                case ASK_EMAIL:
                    if (!isValidEmail(text)) {
                        send(bot, chatId, "Email không hợp lệ. Vui lòng nhập lại:");
                        return;
                    }
                    user.setEmail(text);
                    user.setTelegramId(userId);
                    user.setDateRegistered(OffsetDateTime.now());
                    state.setStep(UserRegistrationStep.CONFIRM);
                    send(bot, chatId, formatUserSummary(user, state.getEventIds()) +
                            "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận, hoặc dùng các lệnh:\n/edit_name\n/edit_phone\n/edit_email để sửa.");
                    break;
                case CONFIRM:
                    System.out.println("In CONFIRM step for user: " + userId + ", received text: " + text);
                    if (!text.equalsIgnoreCase("OK")) {
                        if (user != null) {
                            if (text.equals("/edit_name")) {
                                state.setStep(UserRegistrationStep.EDIT_NAME);
                                send(bot, chatId, "Nhập họ tên mới:");
                            } else if (text.equals("/edit_phone")) {
                                state.setStep(UserRegistrationStep.EDIT_PHONE);
                                send(bot, chatId, "Nhập số điện thoại mới:");
                            } else if (text.equals("/edit_email")) {
                                state.setStep(UserRegistrationStep.EDIT_EMAIL);
                                send(bot, chatId, "Nhập email mới:");
                            } else if (text.equals("/registration_cancel")) {
                                userStates.remove(userId);
                                send(bot, chatId, "Bạn đã hủy quá trình đăng ký.");
                                return;
                            } 
                        } else if (event != null) {
                            if (text.equals("/edit_event_id")) {
                                state.setStep(UserRegistrationStep.EDIT_EVENT_ID);
                                send(bot, chatId, "Nhập Event ID mới:");
                            } else if (text.equals("/edit_event_name")) {
                                state.setStep(UserRegistrationStep.EDIT_EVENT_NAME);
                                send(bot, chatId, "Nhập tên sự kiện mới:");
                            } else if (text.equals("/edit_event_time")) {
                                state.setStep(UserRegistrationStep.EDIT_EVENT_TIME);
                                send(bot, chatId, "Nhập thời gian sự kiện mới (VD: 2025-05-18 18:00):");
                            } else if (text.equals("/edit_event_description")) {
                                state.setStep(UserRegistrationStep.EDIT_EVENT_DESCRIPTION);
                                send(bot, chatId, "Nhập mô tả sự kiện mới:");
                            } else if (text.equals("/edit_event_group_link")) {
                                state.setStep(UserRegistrationStep.EDIT_EVENT_GROUP_LINK);
                                send(bot, chatId, "Nhập link nhóm mới:");
                            } else if (text.equals("/edit_event_capacity")) {
                                state.setStep(UserRegistrationStep.EDIT_EVENT_CAPACITY);
                                send(bot, chatId, "Nhập sức chứa mới (số nguyên):");
                            } else if (text.equals("/event_creation_cancel")) {
                                userStates.remove(userId);
                                send(bot, chatId, "Bạn đã hủy quá trình tạo sự kiện.");
                                return;
                            } else {
                                send(bot, chatId, "Vui lòng gửi 'OK' để xác nhận hoặc dùng /edit_event_id, /edit_event_name, /edit_event_time, " +
                                        "/edit_event_description, /edit_event_group_link, /edit_event_capacity để sửa.");
                            }
                        }
                        return;
                    }

                    if (user != null) {
                        // Handle user registration confirmation
                        User userConfirm = state.getUser();
                        userConfirm.setTelegramId(userId);
                        userConfirm.setDateRegistered(OffsetDateTime.now());
                        boolean userSaved = airtableService.updateOrSaveUser(userConfirm);
                        if (!userSaved) {
                            System.out.println("Failed to save or update user: " + userId);
                            send(bot, chatId, "Lỗi khi lưu thông tin người dùng. Vui lòng thử lại sau.");
                            userStates.remove(userId);
                            return;
                        }

                        List<String> eventIds = state.getEventIds();
                        if (eventIds != null && !eventIds.isEmpty()) {
                            boolean allRegistrationsSaved = true;
                            List<String> failedEvents = new ArrayList<>();
                            for (String eventId : eventIds) {
                                System.out.println("Creating registration for user: " + userId + ", event: " + eventId);
                                Registration reg = new Registration();
                                reg.setUserId(userId.toString());
                                reg.setEventId(eventId);
                                reg.setStatus("Pending");
                                reg.setRegisteredTime(OffsetDateTime.now());
                                System.out.println("Attempting to save registration for event: " + eventId);
                                try {
                                    boolean saved = airtableService.saveRegistration(reg);
                                    System.out.println("Save registration result for event " + eventId + ": " + saved);
                                    if (!saved) {
                                        allRegistrationsSaved = false;
                                        failedEvents.add(eventId);
                                        System.out.println("Failed to save registration for event: " + eventId);
                                    }
                                } catch (Exception e) {
                                    allRegistrationsSaved = false;
                                    failedEvents.add(eventId);
                                    System.out.println("Exception saving registration for event: " + eventId + ": " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }

                            if (!allRegistrationsSaved) {
                                String errorMsg = "Lỗi khi lưu thông tin. Không thể đăng ký các sự kiện: " + String.join(", ", failedEvents) +
                                                ". Vui lòng thử lại sau.";
                                System.out.println("Registration failed: " + errorMsg);
                                send(bot, chatId, errorMsg);
                                userStates.remove(userId);
                                return;
                            }
                        }

                        String successMsg = eventIds != null && !eventIds.isEmpty()
                                ? "Đã lưu thông tin đăng ký sự kiện: " + String.join(", ", eventIds) +
                                  ". Cảm ơn bạn! Vui lòng chờ duyệt từ ban tổ chức."
                                : "Đã cập nhật thông tin người dùng thành công.";
                        System.out.println("Operation completed successfully for user: " + userId + ", events: " + eventIds);
                        send(bot, chatId, successMsg);
                        userStates.remove(userId);
                        } else if (event != null) {
                            // Handle event creation/editing confirmation
                            if (state.getStep() == UserRegistrationStep.ASK_EVENT_ID && airtableService.eventExists(event.getEventId())) {
                                send(bot, chatId, "Event ID đã tồn tại. Vui lòng chọn ID khác hoặc dùng /edit_events để chỉnh sửa.");
                                state.setStep(UserRegistrationStep.ASK_EVENT_ID);
                                return;
                            }
                            boolean eventSaved;
                            boolean isEdit = state.getAirtableRecordId() != null;
                            if (isEdit) {
                                // Existing event update
                                eventSaved = airtableService.updateEventWithRecordId(event, state.getAirtableRecordId());
                            } else {
                                // New event creation
                                eventSaved = airtableService.updateOrSaveEvent(event);
                            }
                            if (!eventSaved) {
                                System.out.println("Failed to save or update event: " + event.getEventId());
                                send(bot, chatId, "Lỗi khi lưu thông tin sự kiện. Vui lòng thử lại sau.");
                                userStates.remove(userId);
                                return;
                            }
                            send(bot, chatId, "Đã lưu thông tin sự kiện thành công: " + event.getEventId());

                            // Notify registered users if event was edited
                            if (isEdit) {
                                Event originalEvent = state.getOriginalEvent();
                                StringBuilder changes = new StringBuilder();
                                if (!Objects.equals(originalEvent.getEventId(), event.getEventId())) {
                                    changes.append("Event ID: ").append(originalEvent.getEventId()).append(" -> ").append(event.getEventId()).append("\n");
                                }
                                if (!Objects.equals(originalEvent.getEventName(), event.getEventName())) {
                                    changes.append("Tên sự kiện: ").append(originalEvent.getEventName()).append(" -> ").append(event.getEventName()).append("\n");
                                }
                                if (!Objects.equals(originalEvent.getTime(), event.getTime())) {
                                    changes.append("Thời gian: ").append(originalEvent.getTime()).append(" -> ").append(event.getTime()).append("\n");
                                }
                                if (!Objects.equals(originalEvent.getDescription(), event.getDescription())) {
                                    changes.append("Mô tả: ").append(originalEvent.getDescription()).append(" -> ").append(event.getDescription()).append("\n");
                                }
                                if (!Objects.equals(originalEvent.getGroupLink(), event.getGroupLink())) {
                                    changes.append("Link nhóm: ").append(originalEvent.getGroupLink()).append(" -> ").append(event.getGroupLink()).append("\n");
                                }
                                if (originalEvent.getCapacity() != event.getCapacity()) {
                                    changes.append("Sức chứa: ").append(originalEvent.getCapacity()).append(" -> ").append(event.getCapacity()).append("\n");
                                }

                                if (changes.length() > 0) {
                                    List<String> registeredUserIds = airtableService.getRegisteredUserIdsForEvent(event.getEventId());
                                    if (!registeredUserIds.isEmpty()) {
                                        String notification = "Sự kiện " + event.getEventId() + " đã được cập nhật:\n" + changes.toString();
                                        for (String registeredUserId : registeredUserIds) {
                                            try {
                                                send(bot, Long.parseLong(registeredUserId), notification);
                                            } catch (NumberFormatException e) {
                                                System.err.println("Invalid user ID format: " + registeredUserId);
                                            }
                                        }
                                        System.out.println("Notified " + registeredUserIds.size() + " users for event update: " + event.getEventId());
                                    } else {
                                        System.out.println("No registered users found for event: " + event.getEventId());
                                    }
                                }
                            }

                            userStates.remove(userId);
                        }
                    break;
                case EDIT_NAME:
                    if (text.isEmpty()) {
                        send(bot, chatId, "Tên không được để trống. Vui lòng nhập lại:");
                        return;
                    }
                    user.setFullName(text);
                    state.setStep(UserRegistrationStep.CONFIRM);
                    send(bot, chatId, formatUserSummary(user, state.getEventIds()) +
                            "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận, hoặc dùng các lệnh:\n/edit_name\n/edit_phone\n/edit_email để sửa.");
                    break;
                case EDIT_PHONE:
                    if (!isValidPhone(text)) {
                        send(bot, chatId, "Số điện thoại không hợp lệ (phải bắt đầu bằng +84 hoặc 0 và có 10 chữ số). Vui lòng nhập lại:");
                        return;
                    }
                    user.setPhoneNumber(text);
                    state.setStep(UserRegistrationStep.CONFIRM);
                    send(bot, chatId, formatUserSummary(user, state.getEventIds()) +
                            "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận, hoặc dùng các lệnh:\n/edit_name\n/edit_phone\n/edit_email để sửa.");
                    break;
                case EDIT_EMAIL:
                    if (!isValidEmail(text)) {
                        send(bot, chatId, "Email không hợp lệ. Vui lòng nhập lại:");
                        return;
                    }
                    user.setEmail(text);
                    state.setStep(UserRegistrationStep.CONFIRM);
                    send(bot, chatId, formatUserSummary(user, state.getEventIds()) +
                            "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận, hoặc dùng các lệnh:\n/edit_name\n/edit_phone\n/edit_email để sửa.");
                    break;
                // Event creation steps
                case ASK_EVENT_ID:
                    if (text.isEmpty()) {
                        send(bot, chatId, "Event ID không được để trống. Vui lòng nhập lại:");
                        return;
                    }
                    if (airtableService.eventExists(text)) {
                        send(bot, chatId, "Event ID đã tồn tại. Vui lòng chọn ID khác hoặc dùng /edit_event để chỉnh sửa.");
                        return;
                    }
                    event.setEventId(text);
                    state.setStep(UserRegistrationStep.ASK_EVENT_NAME);
                    send(bot, chatId, "Nhập tên sự kiện:");
                    break;
                case ASK_EVENT_NAME:
                    if (text.isEmpty()) {
                        send(bot, chatId, "Tên sự kiện không được để trống. Vui lòng nhập lại:");
                        return;
                    }
                    event.setEventName(text);
                    state.setStep(UserRegistrationStep.ASK_EVENT_TIME);
                    send(bot, chatId, "Nhập thời gian sự kiện (VD: 2025-05-18 18:00):");
                    break;
                case ASK_EVENT_TIME:
                    if (text.isEmpty()) {
                        send(bot, chatId, "Thời gian không được để trống. Vui lòng nhập lại:");
                        return;
                    }
                    if (!isValidTimeFormat(text)) {
                        send(bot, chatId, "Định dạng thời gian không hợp lệ. Vui lòng nhập theo định dạng YYYY-MM-DD HH:mm (VD: 2025-05-18 18:00):");
                        return;
                    }
                    event.setTime(text);
                    state.setStep(UserRegistrationStep.ASK_EVENT_DESCRIPTION);
                    send(bot, chatId, "Nhập mô tả sự kiện:");
                    break;
                case ASK_EVENT_DESCRIPTION:
                    if (text.isEmpty()) {
                        send(bot, chatId, "Mô tả không được để trống. Vui lòng nhập lại:");
                        return;
                    }
                    event.setDescription(text);
                    state.setStep(UserRegistrationStep.ASK_EVENT_GROUP_LINK);
                    send(bot, chatId, "Nhập link nhóm (VD: https://t.me/group):");
                    break;
                case ASK_EVENT_GROUP_LINK:
                    if (text.isEmpty()) {
                        send(bot, chatId, "Link nhóm không được để trống. Vui lòng nhập lại:");
                        return;
                    }
                    event.setGroupLink(text);
                    state.setStep(UserRegistrationStep.ASK_EVENT_CAPACITY);
                    send(bot, chatId, "Nhập sức chứa (số nguyên):");
                    break;
                case ASK_EVENT_CAPACITY:
                    int capacity;
                    try {
                        capacity = Integer.parseInt(text);
                        if (capacity <= 0) {
                            send(bot, chatId, "Sức chứa phải là số nguyên dương. Vui lòng nhập lại:");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        send(bot, chatId, "Sức chứa phải là số nguyên. Vui lòng nhập lại:");
                        return;
                    }
                    event.setCapacity(capacity);
                    state.setStep(UserRegistrationStep.CONFIRM);
                    send(bot, chatId, formatEventSummary(event) +
                            "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận, hoặc dùng các lệnh:\n" +
                            "/edit_event_id\n/edit_event_name\n/edit_event_time\n/edit_event_description\n/edit_event_group_link\n/edit_event_capacity để sửa.");
                    break;
                case EDIT_EVENT_ID:
                    if (text.isEmpty()) {
                        send(bot, chatId, "Event ID không được để trống. Vui lòng nhập lại:");
                        return;
                    }
                    if (airtableService.eventExists(text) && !text.equals(state.getEvent().getEventId())) {
                        send(bot, chatId, "Event ID đã tồn tại. Vui lòng chọn ID khác.");
                        return;
                    }
                    String oldEventId = event.getEventId();
                    event.setEventId(text);
                    // Update registrations with the new EventId
                    boolean registrationsUpdated = airtableService.updateRegistrationsEventId(oldEventId, text);
                    if (!registrationsUpdated) {
                        send(bot, chatId, "Lỗi khi cập nhật Event ID trong các đăng ký. Vui lòng thử lại sau.");
                        return;
                    }
                    state.setStep(UserRegistrationStep.CONFIRM);
                    send(bot, chatId, formatEventSummary(event) +
                            "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận, hoặc dùng các lệnh:\n" +
                            "/edit_event_id\n/edit_event_name\n/edit_event_time\n/edit_event_description\n/edit_event_group_link\n/edit_event_capacity để sửa.");
                    break;
                case EDIT_EVENT_NAME:
                    if (text.isEmpty()) {
                        send(bot, chatId, "Tên sự kiện không được để trống. Vui lòng nhập lại:");
                        return;
                    }
                    event.setEventName(text);
                    state.setStep(UserRegistrationStep.CONFIRM);
                    send(bot, chatId, formatEventSummary(event) +
                            "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận, hoặc dùng các lệnh:\n" +
                            "/edit_event_id\n/edit_event_name\n/edit_event_time\n/edit_event_description\n/edit_event_group_link\n/edit_event_capacity để sửa.");
                    break;
                case EDIT_EVENT_TIME:
                    if (text.isEmpty()) {
                        send(bot, chatId, "Thời gian không được để trống. Vui lòng nhập lại:");
                        return;
                    }
                    if (!isValidTimeFormat(text)) {
                        send(bot, chatId, "Định dạng thời gian không hợp lệ. Vui lòng nhập theo định dạng YYYY-MM-DD HH:mm (VD: 2025-05-18 18:00):");
                        return;
                    }
                    event.setTime(text);
                    state.setStep(UserRegistrationStep.CONFIRM);
                    send(bot, chatId, formatEventSummary(event) +
                            "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận, hoặc dùng các lệnh:\n" +
                            "/edit_event_id\n/edit_event_name\n/edit_event_time\n/edit_event_description\n/edit_event_group_link\n/edit_event_capacity để sửa.");
                    break;
                case EDIT_EVENT_DESCRIPTION:
                    if (text.isEmpty()) {
                        send(bot, chatId, "Mô tả không được để trống. Vui lòng nhập lại:");
                        return;
                    }
                    event.setDescription(text);
                    state.setStep(UserRegistrationStep.CONFIRM);
                    send(bot, chatId, formatEventSummary(event) +
                            "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận, hoặc dùng các lệnh:\n" +
                            "/edit_event_id\n/edit_event_name\n/edit_event_time\n/edit_event_description\n/edit_event_group_link\n/edit_event_capacity để sửa.");
                    break;
                case EDIT_EVENT_GROUP_LINK:
                    if (text.isEmpty()) {
                        send(bot, chatId, "Link nhóm không được để trống. Vui lòng nhập lại:");
                        return;
                    }
                    event.setGroupLink(text);
                    state.setStep(UserRegistrationStep.CONFIRM);
                    send(bot, chatId, formatEventSummary(event) +
                            "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận, hoặc dùng các lệnh:\n" +
                            "/edit_event_id\n/edit_event_name\n/edit_event_time\n/edit_event_description\n/edit_event_group_link\n/edit_event_capacity để sửa.");
                    break;
                case EDIT_EVENT_CAPACITY:
                    try {
                        int capacityEdit = Integer.parseInt(text);
                        if (capacityEdit <= 0) {
                            send(bot, chatId, "Sức chứa phải là số nguyên dương. Vui lòng nhập lại:");
                            return;
                        }
                        event.setCapacity(capacityEdit);
                    } catch (NumberFormatException e) {
                        send(bot, chatId, "Sức chứa phải là số nguyên. Vui lòng nhập lại:");
                        return;
                    }
                    state.setStep(UserRegistrationStep.CONFIRM);
                    send(bot, chatId, formatEventSummary(event) +
                            "\n\nThông tin đã đúng chưa? Gửi 'OK' để xác nhận, hoặc dùng các lệnh:\n" +
                            "/edit_event_id\n/edit_event_name\n/edit_event_time\n/edit_event_description\n/edit_event_group_link\n/edit_event_capacity để sửa.");
                    break;
            }
        }
    }

    private void send(MyBot bot, Long chatId, String text) {
        try {
            bot.execute(new SendMessage(chatId.toString(), text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^(\\+84|0)[0-9]{9}$");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
    
    private boolean isValidTimeFormat(String time) {
    try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            formatter.parse(time);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private String formatUserSummary(User user, List<String> eventIds) {
        StringBuilder summary = new StringBuilder();
        summary.append("Thông tin bạn vừa nhập:\n")
               .append("Họ tên: ").append(user.getFullName()).append("\n")
               .append("SĐT: ").append(user.getPhoneNumber()).append("\n")
               .append("Email: ").append(user.getEmail()).append("\n");

        if (eventIds != null && !eventIds.isEmpty()) {
            summary.append("Đã đăng ký Event: \n");
            for (String eventId : eventIds) {
                summary.append(eventId).append("\n");
            }
        }

        return summary.toString();
    }

    private String formatEventSummary(Event event) {
        return "Thông tin sự kiện:\n" +
               "Event ID: " + event.getEventId() + "\n" +
               "Tên sự kiện: " + event.getEventName() + "\n" +
               "Thời gian: " + event.getTime() + "\n" +
               "Mô tả: " + event.getDescription() + "\n" +
               "Link nhóm: " + event.getGroupLink() + "\n" +
               "Sức chứa: " + event.getCapacity();
    }
}