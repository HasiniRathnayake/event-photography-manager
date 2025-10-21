package com.eventphotography.management.controller;

import com.eventphotography.management.entity.User;
import com.eventphotography.management.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        // Get user type from session
        String userType = (String) session.getAttribute("userType");
        
        // Get notifications for the user
        List<?> notifications = notificationService.getUnreadNotifications(currentUser.getUserID());
        model.addAttribute("notifications", notifications);
        
        // Redirect to appropriate dashboard based on user type
        return switch (userType) {
            case "Photographer" -> "redirect:/photographer/dashboard";
            case "Admin" -> "redirect:/admin/dashboard";
            case "Event Organizer" -> "redirect:/event-organizer/dashboard";
            case "Finance Team" -> "redirect:/financial/dashboard";
            case "Customer Support" -> "redirect:/support/dashboard";
            case "Event Attendee" -> "redirect:/attendee/dashboard";
            default -> "redirect:/auth/login";
        };
    }
}