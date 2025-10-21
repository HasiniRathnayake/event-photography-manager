package com.eventphotography.management.util;

import com.eventphotography.management.entity.Notification;
import com.eventphotography.management.entity.User;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class NotificationFactory {

    public Notification createBookingNotification(User user, String message, Integer relatedId) {
        System.out.println("Creating booking notification for user: " + user.getName() + " with message: " + message + " and related ID: " + relatedId);
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("New Booking Notification");
        notification.setMessage(message);
        notification.setType("Booking");
        notification.setRelatedID(relatedId);
        notification.setCreatedDate(LocalDateTime.now());
        notification.setIsRead(false);
        System.out.println("Created notification object");
        return notification;
    }

    public Notification createPaymentNotification(User user, String message, Integer relatedId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Payment Notification");
        notification.setMessage(message);
        notification.setType("Payment");
        notification.setRelatedID(relatedId);
        notification.setCreatedDate(LocalDateTime.now());
        notification.setIsRead(false);
        return notification;
    }

    public Notification createEventNotification(User user, String message, Integer relatedId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Event Update");
        notification.setMessage(message);
        notification.setType("Event");
        notification.setRelatedID(relatedId);
        notification.setCreatedDate(LocalDateTime.now());
        notification.setIsRead(false);
        return notification;
    }

    public Notification createSupportNotification(User user, String message, Integer relatedId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Support Ticket Update");
        notification.setMessage(message);
        notification.setType("Support");
        notification.setRelatedID(relatedId);
        notification.setCreatedDate(LocalDateTime.now());
        notification.setIsRead(false);
        return notification;
    }

    public Notification createSystemNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("System Notification");
        notification.setMessage(message);
        notification.setType("System");
        notification.setCreatedDate(LocalDateTime.now());
        notification.setIsRead(false);
        return notification;
    }
}
