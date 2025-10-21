package com.eventphotography.management.util;

import com.eventphotography.management.entity.User;

/**
 * Utility class for consistent user type checking across the application.
 * This addresses the issue where 'CLIENT' is stored in the database but 
 * 'Event Attendee' is used in the UI.
 */
public class UserTypeChecker {
    
    /**
     * Check if a user is an Event Attendee (Client in database)
     * @param user The user to check
     * @return true if the user is an Event Attendee/Client, false otherwise
     */
    public static boolean isEventAttendee(User user) {
        if (user == null) {
            return false;
        }
        // Check both database value and UI value
        return "CLIENT".equals(user.getUserType()) || "Event Attendee".equals(user.getUserType());
    }
    
    /**
     * Check if a user is a Photographer
     * @param user The user to check
     * @return true if the user is a Photographer, false otherwise
     */
    public static boolean isPhotographer(User user) {
        if (user == null) {
            return false;
        }
        // Check both database value and UI value
        return "PHOTOGRAPHER".equals(user.getUserType()) || "Photographer".equals(user.getUserType());
    }
    
    /**
     * Check if a user is an Admin
     * @param user The user to check
     * @return true if the user is an Admin, false otherwise
     */
    public static boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        // Check both database value and UI value
        return "ADMIN".equals(user.getUserType()) || "Admin".equals(user.getUserType());
    }
    
    /**
     * Check if a user is an Event Organizer
     * @param user The user to check
     * @return true if the user is an Event Organizer, false otherwise
     */
    public static boolean isEventOrganizer(User user) {
        if (user == null) {
            return false;
        }
        // Check both database value and UI value
        return "ORGANIZER".equals(user.getUserType()) || "Event Organizer".equals(user.getUserType());
    }
    
    /**
     * Check if a user is from the Finance team
     * @param user The user to check
     * @return true if the user is from Finance, false otherwise
     */
    public static boolean isFinance(User user) {
        if (user == null) {
            return false;
        }
        // Check both database value and UI value
        return "FINANCE".equals(user.getUserType()) || "Finance".equals(user.getUserType());
    }
    
    /**
     * Check if a user is from Customer Support
     * @param user The user to check
     * @return true if the user is from Customer Support, false otherwise
     */
    public static boolean isCustomerSupport(User user) {
        if (user == null) {
            return false;
        }
        // Check both database value and UI value
        return "SUPPORT".equals(user.getUserType()) || "Customer Support".equals(user.getUserType());
    }
    
    /**
     * Normalize user type to database value
     * @param userType The user type to normalize
     * @return The normalized user type (database value)
     */
    public static String normalizeToDatabaseValue(String userType) {
        if (userType == null) {
            return null;
        }
        
        switch (userType) {
            case "Event Attendee":
                return "CLIENT";
            case "Photographer":
                return "PHOTOGRAPHER";
            case "Admin":
                return "ADMIN";
            case "Event Organizer":
                return "ORGANIZER";
            case "Finance":
                return "FINANCE";
            case "Customer Support":
                return "SUPPORT";
            default:
                return userType; // Already in database format or unknown
        }
    }
    
    /**
     * Normalize user type to UI value
     * @param userType The user type to normalize
     * @return The normalized user type (UI value)
     */
    public static String normalizeToUIValue(String userType) {
        if (userType == null) {
            return null;
        }
        
        switch (userType) {
            case "CLIENT":
                return "Event Attendee";
            case "PHOTOGRAPHER":
                return "Photographer";
            case "ADMIN":
                return "Admin";
            case "ORGANIZER":
                return "Event Organizer";
            case "FINANCE":
                return "Finance";
            case "SUPPORT":
                return "Customer Support";
            default:
                return userType; // Already in UI format or unknown
        }
    }
}