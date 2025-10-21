package com.eventphotography.management.util;

import com.eventphotography.management.entity.User;

public class UserTypeUtils {
    
    /**
     * Check if the user is an event attendee/client
     * @param user The user to check
     * @return true if the user is an event attendee/client, false otherwise
     */
    public static boolean isEventAttendee(User user) {
        if (user == null) {
            return false;
        }
        // Check both the database value (CLIENT) and the session value (Event Attendee)
        return "CLIENT".equals(user.getUserType());
    }
    
    /**
     * Check if the user is an event attendee based on session userType attribute
     * @param sessionUserType The userType from session
     * @return true if the user is an event attendee, false otherwise
     */
    public static boolean isEventAttendeeSession(String sessionUserType) {
        if (sessionUserType == null) {
            return false;
        }
        // Check the session value (Event Attendee)
        return "Event Attendee".equals(sessionUserType);
    }
    
    /**
     * Check if the user is a photographer
     * @param user The user to check
     * @return true if the user is a photographer, false otherwise
     */
    public static boolean isPhotographer(User user) {
        if (user == null) {
            return false;
        }
        return "PHOTOGRAPHER".equals(user.getUserType());
    }
    
    /**
     * Check if the user is a photographer based on session userType attribute
     * @param sessionUserType The userType from session
     * @return true if the user is a photographer, false otherwise
     */
    public static boolean isPhotographerSession(String sessionUserType) {
        if (sessionUserType == null) {
            return false;
        }
        return "Photographer".equals(sessionUserType);
    }
    
    /**
     * Check if the user is an admin
     * @param user The user to check
     * @return true if the user is an admin, false otherwise
     */
    public static boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        return "ADMIN".equals(user.getUserType());
    }
    
    /**
     * Check if the user is an admin based on session userType attribute
     * @param sessionUserType The userType from session
     * @return true if the user is an admin, false otherwise
     */
    public static boolean isAdminSession(String sessionUserType) {
        if (sessionUserType == null) {
            return false;
        }
        return "Admin".equals(sessionUserType);
    }
}