package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findBySender_UserID(Integer senderID);
    List<Message> findByReceiver_UserID(Integer receiverID);
    List<Message> findByIsReadFalse();
}