package com.eventphotography.management.controller;

import com.eventphotography.management.entity.Message;
import com.eventphotography.management.entity.User;
import com.eventphotography.management.service.MessageService;
import com.eventphotography.management.service.UserService;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listMessages(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("receivedMessages", messageService.getMessagesByReceiver(currentUser.getUserID()));
        model.addAttribute("sentMessages", messageService.getMessagesBySender(currentUser.getUserID()));
        model.addAttribute("unreadCount", messageService.getUnreadMessages().size());
        model.addAttribute("user", currentUser);
        return "message/list";
    }

    @GetMapping("/compose")
    public String showComposeForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("message", new Message());
        model.addAttribute("user", currentUser);
        return "message/compose";
    }

    @PostMapping("/send")
    public String sendMessage(HttpSession session,
                             @ModelAttribute Message message,
                             @RequestParam("receiverId") Integer receiverId,
                             Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        try {
            Optional<User> receiverOptional = userService.getUserByIdOptional(receiverId);
            User receiver = receiverOptional.orElse(null);
            if (receiver == null) {
                model.addAttribute("error", "Receiver not found");
                return "message/compose";
            }

            message.setSender(currentUser);
            message.setReceiver(receiver);
            message.setIsRead(false);
            message.setSentDate(java.time.LocalDateTime.now());
            messageService.createMessage(message);

            model.addAttribute("success", "Message sent successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to send message: " + e.getMessage());
        }

        return "redirect:/messages";
    }

    @GetMapping("/{id}")
    public String viewMessage(HttpSession session,
                             @PathVariable Integer id,
                             Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        Message message = messageService.getMessageById(id);
        if (message == null || 
            (!message.getReceiver().getUserID().equals(currentUser.getUserID()) && 
             !message.getSender().getUserID().equals(currentUser.getUserID()))) {
            return "redirect:/messages";
        }

        // Mark as read if the current user is the receiver
        if (message.getReceiver().getUserID().equals(currentUser.getUserID()) && !message.getIsRead()) {
            messageService.markAsRead(id);
        }

        model.addAttribute("message", message);
        model.addAttribute("user", currentUser);
        return "message/view";
    }

    @PostMapping("/delete/{id}")
    public String deleteMessage(HttpSession session,
                                @PathVariable Integer id) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        Message message = messageService.getMessageById(id);
        if (message != null && 
            (message.getReceiver().getUserID().equals(currentUser.getUserID()) || 
             message.getSender().getUserID().equals(currentUser.getUserID()))) {
            messageService.deleteMessage(id);
        }

        return "redirect:/messages";
    }
}