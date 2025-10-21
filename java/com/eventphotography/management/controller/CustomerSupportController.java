package com.eventphotography.management.controller;

import com.eventphotography.management.entity.SupportTicket;
import com.eventphotography.management.entity.User;
import com.eventphotography.management.service.SupportService;
import com.eventphotography.management.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/support")
public class CustomerSupportController {

    @Autowired
    private SupportService supportService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Customer Support".equals(session.getAttribute("userType"))) {
            return "redirect:/auth/login";
        }

        List<SupportTicket> allTickets = supportService.getAllTickets();
        List<SupportTicket> openTickets = supportService.getTicketsByStatus("Open");
        List<SupportTicket> assignedTickets = supportService.getTicketsByAssignedTo(currentUser.getUserID());
        List<SupportTicket> resolvedTickets = supportService.getTicketsByStatus("Resolved");
        
        double resolutionRate = allTickets.isEmpty() ? 0 : 
                (double) resolvedTickets.size() / allTickets.size() * 100;

        model.addAttribute("totalTickets", allTickets.size());
        model.addAttribute("openTickets", openTickets.size());
        model.addAttribute("assignedTickets", assignedTickets.size());
        model.addAttribute("resolutionRate", resolutionRate);
        model.addAttribute("recentTickets", allTickets.stream().limit(5).toList());

        return "customer-support/dashboard";
    }

    @GetMapping("/tickets")
    public String viewTickets(Model model, HttpSession session,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String priority) {
        User currentUser = (User) session.getAttribute("currentUser");
        List<SupportTicket> tickets;

        if ("Customer Support".equals(session.getAttribute("userType"))) {
            tickets = supportService.getAllTickets();
        } else {
            tickets = supportService.getTicketsByUser(currentUser.getUserID());
        }
        
        // Apply search filter
        if (search != null && !search.isEmpty()) {
            tickets = tickets.stream()
                .filter(ticket -> ticket.getSubject().toLowerCase().contains(search.toLowerCase()) ||
                        ticket.getDescription().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }
        
        // Apply status filter
        if (status != null && !status.isEmpty() && !"All Statuses".equals(status)) {
            tickets = tickets.stream()
                .filter(ticket -> status.equals(ticket.getStatus()))
                .toList();
        }
        
        // Apply priority filter
        if (priority != null && !priority.isEmpty() && !"All Priorities".equals(priority)) {
            tickets = tickets.stream()
                .filter(ticket -> priority.equals(ticket.getPriority()))
                .toList();
        }

        model.addAttribute("tickets", tickets);
        model.addAttribute("searchQuery", search);
        model.addAttribute("statusFilter", status);
        model.addAttribute("priorityFilter", priority);
        return "customer-support/tickets";
    }

    @GetMapping("/tickets/create")
    public String showCreateTicketForm(Model model) {
        model.addAttribute("ticket", new SupportTicket());
        return "customer-support/create-ticket";
    }

    @PostMapping("/tickets/create")
    public String createTicket(@RequestParam String subject,
                               @RequestParam String description,
                               @RequestParam(required = false) Integer bookingId,
                               HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        supportService.createTicket(currentUser.getUserID(), subject, description, bookingId);
        return "redirect:/support/tickets";
    }

    @GetMapping("/tickets/{ticketId}/edit")
    public String showEditTicketForm(@PathVariable Integer ticketId, Model model) {
        SupportTicket ticket = supportService.getTicketById(ticketId);
        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("ticket", ticket);
        model.addAttribute("allUsers", allUsers);
        return "customer-support/edit-ticket";
    }

    @PostMapping("/tickets/{ticketId}/update")
    public String updateTicket(@PathVariable Integer ticketId,
                               @ModelAttribute SupportTicket ticket,
                               @RequestParam(required = false) Integer assignedTo,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            // If validation fails, return to the edit form with error messages
            List<User> allUsers = userService.getAllUsers();
            model.addAttribute("ticket", ticket);
            model.addAttribute("allUsers", allUsers);
            return "customer-support/edit-ticket";
        }
        
        SupportTicket existingTicket = supportService.getTicketById(ticketId);
        existingTicket.setSubject(ticket.getSubject());
        existingTicket.setDescription(ticket.getDescription());
        existingTicket.setPriority(ticket.getPriority());
        existingTicket.setStatus(ticket.getStatus());
        
        // Update assignedTo field
        if (assignedTo != null) {
            // Using Optional properly
            Optional<User> assignedUserOptional = userService.getUserByIdOptional(assignedTo);
            User assignedUser = assignedUserOptional.orElse(null);
            existingTicket.setAssignedTo(assignedUser);
        } else {
            existingTicket.setAssignedTo(null);
        }
        
        // Update other fields as needed
        supportService.updateTicket(existingTicket);
        return "redirect:/support/tickets";
    }

    @PostMapping("/tickets/{ticketId}/delete")
    public String deleteTicket(@PathVariable Integer ticketId) {
        supportService.deleteTicket(ticketId);
        return "redirect:/support/tickets";
    }

    @PostMapping("/tickets/{ticketId}/assign")
    public String assignTicket(@PathVariable Integer ticketId, 
                               @RequestParam(required = false) String assigneeName,
                               HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (assigneeName != null && !assigneeName.isEmpty()) {
            // Find user by name and assign to them
            List<User> users = userService.getAllUsers();
            User assignee = users.stream()
                .filter(user -> user.getName().equalsIgnoreCase(assigneeName))
                .findFirst()
                .orElse(null);
                
            if (assignee != null) {
                supportService.assignTicket(ticketId, assignee.getUserID());
            }
        } else {
            // Assign to current user (default behavior)
            supportService.assignTicket(ticketId, currentUser.getUserID());
        }
        
        return "redirect:/support/tickets";
    }

    @PostMapping("/tickets/{ticketId}/resolve")
    public String resolveTicket(@PathVariable Integer ticketId,
                                @RequestParam String resolutionNotes) {
        supportService.resolveTicket(ticketId, resolutionNotes);
        return "redirect:/support/tickets";
    }

    @GetMapping("/tickets/{ticketId}")
    public String viewTicket(@PathVariable Integer ticketId, Model model) {
        SupportTicket ticket = supportService.getTicketById(ticketId);
        model.addAttribute("ticket", ticket);
        return "customer-support/ticket-details";
    }

    @PostMapping("/tickets/{ticketId}/update-priority")
    public String updateTicketPriority(@PathVariable Integer ticketId,
                                       @RequestParam String priority) {
        SupportTicket ticket = supportService.getTicketById(ticketId);
        ticket.setPriority(priority);
        supportService.updateTicket(ticket); // Save the updated ticket
        return "redirect:/support/tickets/" + ticketId;
    }

    @GetMapping("/reports")
    public String viewReports(Model model) {
        List<SupportTicket> allTickets = supportService.getAllTickets();
        List<SupportTicket> resolvedTickets = supportService.getTicketsByStatus("Resolved");
        List<SupportTicket> openTickets = supportService.getTicketsByStatus("Open");
        
        // Get recent tickets for activity display
        List<SupportTicket> recentTickets = allTickets.stream()
            .sorted((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate()))
            .limit(10)
            .toList();

        model.addAttribute("totalTickets", allTickets.size());
        model.addAttribute("resolvedTickets", resolvedTickets.size());
        model.addAttribute("openTickets", openTickets.size());
        model.addAttribute("resolutionRate", allTickets.isEmpty() ? 0 :
                (double) resolvedTickets.size() / allTickets.size() * 100);
        model.addAttribute("recentTickets", recentTickets);

        return "customer-support/reports";
    }
}