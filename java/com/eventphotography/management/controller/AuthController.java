package com.eventphotography.management.controller;

import com.eventphotography.management.entity.User;
import com.eventphotography.management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("userTypes", new String[]{
                "Admin", "Photographer", "Event Organizer", "Finance Team", "Customer Support", "Event Attendee"
        });
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam String userType,
                        HttpSession session,
                        Model model) {

        // Map user-friendly names to database values
        String dbUserType = mapUserTypeToDatabaseValue(userType);
        
        System.out.println("Login attempt - Email: " + email + ", User Type: " + userType + ", DB User Type: " + dbUserType);

        Optional<User> userOpt = userService.authenticateUser(email, password, dbUserType);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("Login successful for user ID: " + user.getUserID());
            session.setAttribute("currentUser", user);
            session.setAttribute("userType", userType); // Keep the user-friendly name in session
            
            System.out.println("Session attributes set - User Type: " + userType);

            return switch (userType) {
                case "Photographer" -> {
                    System.out.println("Redirecting photographer to dashboard");
                    yield "redirect:/photographer/dashboard";
                }
                case "Admin" -> "redirect:/admin/dashboard";
                case "Event Organizer" -> "redirect:/event-organizer/dashboard";
                case "Finance Team" -> "redirect:/financial/dashboard";
                case "Customer Support" -> "redirect:/support/dashboard";
                case "Event Attendee" -> "redirect:/attendee/dashboard";
                default -> "redirect:/auth/login?error=true";
            };
        } else {
            System.out.println("Login failed for email: " + email);
            model.addAttribute("error", "Invalid credentials or user type mismatch");
            model.addAttribute("userTypes", new String[]{
                    "Admin", "Photographer", "Event Organizer", "Finance Team", "Customer Support", "Event Attendee"
            });
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("userTypes", new String[]{
                "Admin", "Photographer", "Event Organizer", "Finance Team", "Customer Support", "Event Attendee"
        });
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String contactNo,
                           @RequestParam String address,
                           @RequestParam String userType,
                           Model model) {

        try {
            // Map user-friendly names to database values
            String dbUserType = mapUserTypeToDatabaseValue(userType);
            
            User user = userService.registerUser(name, email, password, contactNo, address, dbUserType);
            model.addAttribute("success", "Account successfully created! Please login.");
            model.addAttribute("userTypes", new String[]{
                    "Admin", "Photographer", "Event Organizer", "Finance Team", "Customer Support", "Event Attendee"
            });
            return "auth/login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("userTypes", new String[]{
                    "Admin", "Photographer", "Event Organizer", "Finance Team", "Customer Support", "Event Attendee"
            });
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
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
}