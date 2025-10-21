package com.eventphotography.management.service;

import com.eventphotography.management.entity.Client;
import com.eventphotography.management.entity.Photographer;
import com.eventphotography.management.entity.User;
import com.eventphotography.management.repository.ClientRepository;
import com.eventphotography.management.repository.PhotographerRepository;
import com.eventphotography.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PhotographerRepository photographerRepository;
    
    @Autowired
    private ClientRepository clientRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public Optional<User> getUserByIdOptional(Integer userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user != null ? Optional.of(user) : Optional.empty();
    }

    public List<User> getUsersByUserType(String userType) {
        return userRepository.findByUserType(userType);
    }

    public Optional<User> authenticateUser(String email, String password, String userType) {
        User user = userRepository.findByEmailAndPassword(email, password);
        if (user != null && userType.equals(user.getUserType())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Transactional
    public User createUser(User user) {
        user.setCreatedDate(LocalDateTime.now());
        user.setIsActive(true);
        return userRepository.save(user);
    }

    @Transactional
    public User registerUser(String name, String email, String password, String contactNo, String address, String userType) {
        // Check if user already exists
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            throw new RuntimeException("User with this email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password); // In a real application, this should be hashed
        user.setContactNo(contactNo);
        user.setAddress(address);
        user.setUserType(userType);
        user.setCreatedDate(LocalDateTime.now());
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        
        // If the user is registering as a photographer, also create a Photographer entity
        if ("PHOTOGRAPHER".equals(userType)) {
            try {
                Photographer photographer = new Photographer();
                photographer.setUser(savedUser);
                photographer.setRating(java.math.BigDecimal.ZERO);
                photographer.setPricePerHour(java.math.BigDecimal.ZERO);
                photographer.setIsApproved(false); // New photographers need approval
                photographerRepository.save(photographer);
                System.out.println("Photographer entity created for user ID: " + savedUser.getUserID());
            } catch (Exception e) {
                System.err.println("Error creating Photographer entity: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // If the user is registering as a client, also create a Client entity
        if ("CLIENT".equals(userType)) {
            try {
                Client client = new Client();
                client.setUser(savedUser);
                clientRepository.save(client);
                System.out.println("Client entity created for user ID: " + savedUser.getUserID());
            } catch (Exception e) {
                System.err.println("Error creating Client entity: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return savedUser;
    }

    @Transactional
    public User updateUser(User user) {
        // Preserve the existing password if it's not provided (null or empty)
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            User existingUser = userRepository.findById(user.getUserID()).orElse(null);
            if (existingUser != null) {
                user.setPassword(existingUser.getPassword());
                // Preserve the createdDate
                user.setCreatedDate(existingUser.getCreatedDate());
            }
        }
        return userRepository.save(user);
    }

    @Transactional
    public boolean deleteUser(Integer userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    @Transactional
    public User deactivateUser(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setIsActive(false);
            return userRepository.save(user);
        }
        return null;
    }

    @Transactional
    public User activateUser(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setIsActive(true);
            return userRepository.save(user);
        }
        return null;
    }

    public long getUserCount() {
        return userRepository.count();
    }

    public long getUserCountByType(String userType) {
        return userRepository.findByUserType(userType).size();
    }
    
    public Client getClientByUserId(Integer userId) {
        return clientRepository.findByUserId(userId);
    }
    
    // Method to manually create a photographer entity for existing users
    @Transactional
    public void createPhotographerForUser(User user) {
        if ("PHOTOGRAPHER".equals(user.getUserType()) && photographerRepository.findByUserId(user.getUserID()) == null) {
            try {
                Photographer photographer = new Photographer();
                photographer.setUser(user);
                photographer.setRating(java.math.BigDecimal.ZERO);
                photographer.setPricePerHour(java.math.BigDecimal.ZERO);
                photographer.setIsApproved(false); // New photographers need approval
                photographerRepository.save(photographer);
                System.out.println("Photographer entity manually created for user ID: " + user.getUserID());
            } catch (Exception e) {
                System.err.println("Error manually creating Photographer entity: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Method to manually create a client entity for existing users
    @Transactional
    public void createClientForUser(User user) {
        if ("CLIENT".equals(user.getUserType()) && clientRepository.findByUserId(user.getUserID()) == null) {
            try {
                Client client = new Client();
                client.setUser(user);
                clientRepository.save(client);
                System.out.println("Client entity manually created for user ID: " + user.getUserID());
            } catch (Exception e) {
                System.err.println("Error manually creating Client entity: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}