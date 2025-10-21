package com.eventphotography.management.controller;

import com.eventphotography.management.entity.*;
import com.eventphotography.management.service.AdminService;
import com.eventphotography.management.service.EventService;
import com.eventphotography.management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Admin".equals(session.getAttribute("userType"))) {
            return "redirect:/auth/login";
        }

        List<User> allUsers = adminService.getAllUsers();
        List<Photographer> pendingApprovals = adminService.getPhotographersForApproval();
        List<Booking> allBookings = adminService.getAllBookings();
        List<Event> allEvents = adminService.getAllEvents();

        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("pendingApprovals", pendingApprovals.size());
        model.addAttribute("totalBookings", allBookings.size());
        model.addAttribute("totalEvents", allEvents.size());
        model.addAttribute("pendingPhotographers", pendingApprovals);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = adminService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam String contactNo,
                            @RequestParam String address,
                            @RequestParam String userType,
                            Model model) {
        try {
            // Map user-friendly names to database values
            String dbUserType = mapUserTypeToDatabaseValue(userType);
            userService.registerUser(name, email, password, contactNo, address, dbUserType);
            return "redirect:/admin/users?success=User created successfully";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            List<User> users = adminService.getAllUsers();
            model.addAttribute("users", users);
            return "admin/users";
        }
    }
    
    // Helper method to map user-friendly names to database values
    private String mapUserTypeToDatabaseValue(String userType) {
        return switch (userType) {
            case "Photographer" -> "PHOTOGRAPHER";
            case "Admin" -> "ADMIN";
            case "Event Organizer" -> "ORGANIZER";
            case "Finance Team" -> "FINANCE";
            case "Customer Support" -> "SUPPORT";
            case "Event Attendee" -> "CLIENT";
            default -> userType; // Return as-is if no mapping found
        };
    }

    @GetMapping("/users/{userId}")
    public String viewUser(@PathVariable Integer userId, Model model) {
        User user = userService.getUserByIdOptional(userId).orElse(null);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        return "admin/view-user";
    }

    @GetMapping("/users/{userId}/edit")
    public String editUserForm(@PathVariable Integer userId, Model model) {
        User user = userService.getUserByIdOptional(userId).orElse(null);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        return "admin/edit-user";
    }

    @PostMapping("/users/{userId}/update")
    public String updateUser(@PathVariable Integer userId, 
                            @ModelAttribute User user) {
        userService.updateUser(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/deactivate")
    public String deactivateUser(@PathVariable Integer userId) {
        userService.deactivateUser(userId);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Integer userId) {
        adminService.deleteUser(userId);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/update-role")
    public String updateUserRole(@PathVariable Integer userId, @RequestParam String newRole) {
        adminService.updateUserRole(userId, newRole);
        return "redirect:/admin/users";
    }

    @GetMapping("/photographers/approval")
    public String photographerApproval(Model model) {
        List<Photographer> pendingPhotographers = adminService.getPhotographersForApproval();
        model.addAttribute("photographers", pendingPhotographers);
        return "admin/photographer-approval";
    }

    @PostMapping("/photographers/{photographerId}/approve")
    public String approvePhotographer(@PathVariable Integer photographerId) {
        adminService.approvePhotographer(photographerId);
        return "redirect:/admin/photographers/approval";
    }

    @GetMapping("/bookings")
    public String manageBookings(Model model) {
        List<Booking> bookings = adminService.getAllBookings();
        model.addAttribute("bookings", bookings);
        return "admin/bookings";
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public String cancelBooking(@PathVariable Integer bookingId) {
        adminService.cancelBooking(bookingId);
        return "redirect:/admin/bookings";
    }

    @PostMapping("/bookings/{bookingId}/reassign")
    public String reassignBooking(@PathVariable Integer bookingId,
                                  @RequestParam Integer newPhotographerId) {
        adminService.reassignBooking(bookingId, newPhotographerId);
        return "redirect:/admin/bookings";
    }

    @GetMapping("/events")
    public String manageEvents(Model model) {
        List<Event> events = adminService.getAllEvents();
        model.addAttribute("events", events);
        return "admin/events";
    }

    @GetMapping("/events/{eventId}")
    public String viewEvent(@PathVariable Integer eventId, Model model) {
        try {
            Event event = eventService.getEventById(eventId);
            model.addAttribute("event", event);
            return "admin/view-event";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading event: " + e.getMessage());
            return "redirect:/admin/events";
        }
    }

    @GetMapping("/system")
    public String systemSettings(Model model) {
        return "admin/system-settings";
    }
}