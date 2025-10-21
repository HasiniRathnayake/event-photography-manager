package com.eventphotography.management.service;

import com.eventphotography.management.entity.Notification;
import com.eventphotography.management.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Concrete Observer that sends email notifications when events occur
 * Implements the NotificationObserver interface
 */
@Component
public class EmailNotificationObserver implements NotificationService.NotificationObserver {
    
    @Autowired
    private EmailService emailService;

    @Override
    public void update(String eventType, Object data) {
        if (data instanceof Notification) {
            Notification notification = (Notification) data;
            User user = notification.getUser();
            
            if (user != null && user.getEmail() != null) {
                String subject = "Event Photography Management - " + notification.getTitle();
                String text = "Hello " + user.getName() + ",\n\n" + 
                             notification.getMessage() + "\n\n" +
                             "Please log in to your dashboard for more details.";
                
                try {
                    emailService.sendSimpleMessage(user.getEmail(), subject, text);
                    System.out.println("Email notification sent to: " + user.getEmail() + " for event type: " + eventType);
                } catch (Exception e) {
                    System.err.println("Failed to send email notification to: " + user.getEmail() + ". Error: " + e.getMessage());
                }
            }
        }
    }
}