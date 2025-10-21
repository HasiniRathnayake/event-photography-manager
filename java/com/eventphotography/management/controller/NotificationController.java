package com.eventphotography.management.controller;

import com.eventphotography.management.entity.Notification;
import com.eventphotography.management.entity.User;
import com.eventphotography.management.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listNotifications(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        List<Notification> notifications = notificationService.getUserNotifications(currentUser.getUserID());
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notifications.stream().filter(n -> !n.getIsRead()).count());
        
        return "notifications/list";
    }

    @PostMapping("/{notificationId}/mark-as-read")
    public String markAsRead(@PathVariable Integer notificationId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        notificationService.markAsRead(notificationId);
        return "redirect:/notifications";
    }

    @PostMapping("/mark-all-as-read")
    public String markAllAsRead(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        notificationService.markAllAsRead(currentUser.getUserID());
        return "redirect:/notifications";
    }
}