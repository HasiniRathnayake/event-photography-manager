package com.eventphotography.management.service;

import com.eventphotography.management.entity.Notification;
import com.eventphotography.management.entity.User;
import com.eventphotography.management.repository.NotificationRepository;
import com.eventphotography.management.util.NotificationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService implements NotificationSubject {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationFactory notificationFactory;
    
    // Removed EmailNotificationObserver dependency
    
    @Autowired
    private LoggingNotificationObserver loggingObserver;

    private List<NotificationObserver> observers = new ArrayList<>();

    @PostConstruct
    public void init() {
        // Register default observers
        subscribe(loggingObserver);
        System.out.println("Notification observers registered: Logging only");
    }

    public interface NotificationObserver {
        void update(String eventType, Object data);
    }

    @Override
    public void subscribe(NotificationObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(NotificationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String eventType, Object data) {
        for (NotificationObserver observer : observers) {
            observer.update(eventType, data);
        }
    }

    public void notifyNewBooking(User photographer, Integer bookingId) {
        System.out.println("Creating booking notification for user: " + photographer.getName() + " with booking ID: " + bookingId);
        Notification notification = notificationFactory.createBookingNotification(
                photographer,
                "You have a new booking assigned to you.",
                bookingId
        );
        notificationRepository.save(notification);
        System.out.println("Saved notification with ID: " + notification.getNotificationID());
        notifyObservers("NEW_BOOKING", notification);
    }

    public void notifyPaymentUpdate(User client, Integer invoiceId) {
        Notification notification = notificationFactory.createPaymentNotification(
                client,
                "Your invoice payment status has been updated.",
                invoiceId
        );
        notificationRepository.save(notification);
        notifyObservers("PAYMENT_UPDATE", notification);
    }

    public void notifyEventUpdate(User user, String message, Integer eventId) {
        Notification notification = notificationFactory.createEventNotification(
                user,
                message,
                eventId
        );
        notificationRepository.save(notification);
        notifyObservers("EVENT_UPDATE", notification);
    }

    public void notifySupportUpdate(User user, String message, Integer ticketId) {
        Notification notification = notificationFactory.createSupportNotification(
                user,
                message,
                ticketId
        );
        notificationRepository.save(notification);
        notifyObservers("SUPPORT_UPDATE", notification);
    }

    public void notifyReviewUpdate(User user, String message, Integer reviewId) {
        Notification notification = notificationFactory.createSystemNotification(
                user,
                message
        );
        notification.setRelatedID(reviewId);
        notification.setTitle("Review Notification");
        notificationRepository.save(notification);
        notifyObservers("REVIEW_UPDATE", notification);
    }

    public void notifyAlbumUpdate(User user, String message, Integer albumId) {
        Notification notification = notificationFactory.createEventNotification(
                user,
                message,
                albumId
        );
        notification.setTitle("Album Notification");
        notificationRepository.save(notification);
        notifyObservers("ALBUM_UPDATE", notification);
    }

    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserOrderByCreatedDateDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Integer userId) {
        return notificationRepository.findUnreadByUser(userId);
    }

    public void markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    public void markAllAsRead(Integer userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }
}