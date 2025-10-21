package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    @Query("SELECT n FROM Notification n WHERE n.user.userID = :userID ORDER BY n.createdDate DESC")
    List<Notification> findByUserOrderByCreatedDateDesc(@Param("userID") Integer userID);

    @Query("SELECT n FROM Notification n WHERE n.user.userID = :userID AND n.isRead = false")
    List<Notification> findUnreadByUser(@Param("userID") Integer userID);
}
