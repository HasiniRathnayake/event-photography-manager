package com.eventphotography.management.service;

import com.eventphotography.management.entity.Message;
import com.eventphotography.management.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public List<Message> getMessagesBySender(Integer senderId) {
        return messageRepository.findBySender_UserID(senderId);
    }

    public List<Message> getMessagesByReceiver(Integer receiverId) {
        return messageRepository.findByReceiver_UserID(receiverId);
    }

    public List<Message> getUnreadMessages() {
        return messageRepository.findByIsReadFalse();
    }

    public Message getMessageById(Integer messageId) {
        return messageRepository.findById(messageId).orElse(null);
    }

    @Transactional
    public Message createMessage(Message message) {
        return messageRepository.save(message);
    }

    @Transactional
    public Message updateMessage(Message message) {
        return messageRepository.save(message);
    }

    @Transactional
    public boolean deleteMessage(Integer messageId) {
        if (messageRepository.existsById(messageId)) {
            messageRepository.deleteById(messageId);
            return true;
        }
        return false;
    }

    @Transactional
    public void markAsRead(Integer messageId) {
        Message message = getMessageById(messageId);
        if (message != null) {
            message.setIsRead(true);
            messageRepository.save(message);
        }
    }
}