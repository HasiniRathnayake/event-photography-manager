package com.eventphotography.management.service;

import com.eventphotography.management.service.NotificationService.NotificationObserver;

/**
 * Subject interface for the Observer pattern implementation
 * Defines methods for managing observers
 */
public interface NotificationSubject {
    void subscribe(NotificationObserver observer);
    void unsubscribe(NotificationObserver observer);
    void notifyObservers(String eventType, Object data);
}