package com.eventphotography.management.service;

import com.eventphotography.management.entity.Notification;
import org.springframework.stereotype.Component;

/**
 * Concrete Observer that logs notification events
 * Implements the NotificationObserver interface
 */
@Component
public class LoggingNotificationObserver implements NotificationService.NotificationObserver {

    @Override
    public void update(String eventType, Object data) {
        if (data instanceof Notification) {
            Notification notification = (Notification) data;
            System.out.println("LOG: Notification Event - Type: " + eventType + 
                              ", Title: " + notification.getTitle() + 
                              ", Message: " + notification.getMessage() + 
                              ", User ID: " + notification.getUser().getUserID());
        } else {
            System.out.println("LOG: Notification Event - Type: " + eventType + ", Data: " + data);
        }
    }
}