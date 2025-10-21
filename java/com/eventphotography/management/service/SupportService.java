package com.eventphotography.management.service;

import com.eventphotography.management.entity.SupportTicket;
import com.eventphotography.management.entity.User;
import com.eventphotography.management.repository.SupportTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SupportService {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;

    public SupportTicket createTicket(Integer userId, String subject, String description, Integer eventId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        SupportTicket ticket = new SupportTicket();
        ticket.setUser(user);
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setStatus("Open");
        ticket.setPriority("Medium");
        ticket.setCreatedDate(LocalDateTime.now());

        // Note: The SupportTicket entity doesn't have a direct event relationship
        // In a more complete implementation, you might want to link to a specific booking

        SupportTicket savedTicket = supportTicketRepository.save(ticket);
        
        // Notify the user who created the ticket
        notificationService.notifySupportUpdate(user, "Your support ticket '" + subject + "' has been created.", savedTicket.getTicketID());
        
        // Notify all customer support team members
        List<User> supportTeam = userService.getUsersByUserType("SUPPORT");
        for (User supportMember : supportTeam) {
            if (!supportMember.getUserID().equals(user.getUserID())) { // Don't notify the ticket creator
                notificationService.notifySupportUpdate(supportMember, 
                    "New support ticket created by " + user.getName() + ": '" + subject + "'", savedTicket.getTicketID());
            }
        }
        
        return savedTicket;
    }

    public List<SupportTicket> getAllTickets() {
        return supportTicketRepository.findAll();
    }

    public List<SupportTicket> getTicketsByStatus(String status) {
        return supportTicketRepository.findByStatus(status);
    }

    public SupportTicket resolveTicket(Integer ticketId, String resolutionNotes) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus("Resolved");
        ticket.setResolvedDate(LocalDateTime.now());
        ticket.setDescription(ticket.getDescription() + "\n\nResolution: " + resolutionNotes);

        SupportTicket resolvedTicket = supportTicketRepository.save(ticket);
        
        // Notify the user who created the ticket
        notificationService.notifySupportUpdate(ticket.getUser(), 
            "Your support ticket '" + ticket.getSubject() + "' has been resolved.", ticketId);
        
        return resolvedTicket;
    }

    public List<SupportTicket> getTicketsByUser(Integer userId) {
        return supportTicketRepository.findByUser(userId);
    }

    public List<SupportTicket> getTicketsByAssignedTo(Integer userId) {
        return supportTicketRepository.findByAssignedTo(userId);
    }

    public SupportTicket getTicketById(Integer ticketId) {
        Optional<SupportTicket> ticket = supportTicketRepository.findById(ticketId);
        return ticket.orElse(null);
    }

    public SupportTicket updateTicket(SupportTicket ticket) {
        SupportTicket updatedTicket = supportTicketRepository.save(ticket);
        
        // Notify relevant parties about the ticket update
        if (ticket.getAssignedTo() != null) {
            notificationService.notifySupportUpdate(ticket.getAssignedTo(), 
                "Support ticket '" + ticket.getSubject() + "' has been updated.", ticket.getTicketID());
        }
        
        return updatedTicket;
    }

    public void deleteTicket(Integer ticketId) {
        supportTicketRepository.deleteById(ticketId);
    }

    public SupportTicket assignTicket(Integer ticketId, Integer userId) {
        Optional<SupportTicket> ticketOptional = supportTicketRepository.findById(ticketId);
        if (ticketOptional.isPresent()) {
            SupportTicket ticket = ticketOptional.get();
            User user = userService.getUserByIdOptional(userId).orElse(null);
            if (user != null) {
                ticket.setAssignedTo(user);
                ticket.setStatus("In Progress");
                SupportTicket assignedTicket = supportTicketRepository.save(ticket);
                
                // Notify the assigned user
                notificationService.notifySupportUpdate(user, 
                    "You have been assigned to support ticket '" + ticket.getSubject() + "'.", ticketId);
                
                return assignedTicket;
            }
        }
        return null;
    }
}