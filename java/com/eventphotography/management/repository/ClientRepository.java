package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Client;
import com.eventphotography.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    
    @Query("SELECT c FROM Client c WHERE c.user.userID = :userId")
    Client findByUserId(@Param("userId") Integer userId);
}