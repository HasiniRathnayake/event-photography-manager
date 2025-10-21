package com.eventphotography.management.repository;

import com.eventphotography.management.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Integer> {
    List<SupportTicket> findByStatus(String status);

    @Query("SELECT st FROM SupportTicket st WHERE st.assignedTo.userID = :assignedTo")
    List<SupportTicket> findByAssignedTo(@Param("assignedTo") Integer assignedTo);

    @Query("SELECT st FROM SupportTicket st WHERE st.user.userID = :userID")
    List<SupportTicket> findByUser(@Param("userID") Integer userID);
}