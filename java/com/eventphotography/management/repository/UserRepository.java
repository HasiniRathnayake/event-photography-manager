package com.eventphotography.management.repository;

import com.eventphotography.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
    List<User> findByUserType(String userType);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.password = :password")
    User findByEmailAndPassword(@Param("email") String email, @Param("password") String password);
    
    @Query("SELECT u FROM User u WHERE u.userType = 'CLIENT' AND u.isActive = true")
    List<User> findActiveClients();
}