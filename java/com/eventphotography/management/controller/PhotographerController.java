package com.eventphotography.management.controller;

import com.eventphotography.management.entity.*;
import com.eventphotography.management.service.PhotoService;
import com.eventphotography.management.service.PhotographerService;
import com.eventphotography.management.service.FinancialService;
import com.eventphotography.management.service.UserService;
import com.eventphotography.management.service.BookingService;
import com.eventphotography.management.service.EventService;
import com.eventphotography.management.repository.AlbumRepository;
import com.eventphotography.management.util.UserTypeChecker;
import com.eventphotography.management.util.AlbumStatusChecker;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/photographer")
public class PhotographerController {

    @Autowired
    private PhotographerService photographerService;

    @Autowired
    private FinancialService financialService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private AlbumRepository albumRepository;
    
    @Autowired
    private PhotoService photoService;
    
    @Autowired
    private AlbumStatusChecker albumStatusChecker;

    // Dashboard
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        String userType = (String) session.getAttribute("userType");
        
        System.out.println("Photographer dashboard access attempt:");
        System.out.println("Current user: " + currentUser);
        System.out.println("User type in session: " + userType);
        
        if (currentUser == null) {
            System.out.println("Redirecting to login - User not authenticated");
            return "redirect:/auth/login";
        }
        
        if (!UserTypeChecker.isPhotographer(currentUser)) {
            System.out.println("Redirecting to login - User not a photographer");
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        System.out.println("Total photographers in database: " + photographerService.getAllPhotographers().size());
        System.out.println("Photographer found: " + photographer);
        if (photographer != null) {
            System.out.println("Photographer rating: " + photographer.getRating());
        }
        
        if (photographer == null) {
            System.out.println("No photographer entity found, trying to create one...");
            // Try to create a photographer entity for this user
            userService.createPhotographerForUser(currentUser);
            // Try again
            photographer = photographerService.getPhotographerByUser(currentUser);
            if (photographer == null) {
                System.out.println("Still no photographer entity found after creation attempt");
                return "redirect:/auth/login";
            } else {
                System.out.println("Successfully created photographer entity");
            }
        }
        
        // Refresh the photographer entity to get the latest rating
        System.out.println("Refreshing photographer entity to get latest rating");
        photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer != null) {
            System.out.println("Refreshed photographer rating: " + photographer.getRating());
        }

        model.addAttribute("photographer", photographer);
        model.addAttribute("upcomingEvents", photographerService.getUpcomingEvents(photographer.getPhotographerID()));
        model.addAttribute("pastEvents", photographerService.getPastEventsForAlbumCreation(photographer.getPhotographerID()));
        model.addAttribute("recentReviews", photographerService.getRecentReviews(photographer.getPhotographerID()));
        model.addAttribute("albums", photographerService.getAlbumsByPhotographer(photographer.getPhotographerID()));

        return "photographer/dashboard";
    }

    // Profile management
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }
        
        System.out.println("Showing profile for photographer: " + photographer.getUser().getName());
        System.out.println("Photographer rating: " + photographer.getRating());

        // Calculate performance stats
        List<Event> upcomingEvents = photographerService.getUpcomingEvents(photographer.getPhotographerID());
        List<Event> pastEvents = photographerService.getPastEventsForAlbumCreation(photographer.getPhotographerID());
        List<Album> albums = photographerService.getAlbumsByPhotographer(photographer.getPhotographerID());
        List<Review> reviews = photographerService.getRecentReviews(photographer.getPhotographerID());
        
        // Calculate total photos
        int totalPhotos = 0;
        for (Album album : albums) {
            List<Photo> albumPhotos = photoService.getPhotosByAlbum(album.getAlbumID());
            totalPhotos += albumPhotos.size();
        }
        // Calculate average rating
        BigDecimal averageRating = BigDecimal.ZERO;
        if (!reviews.isEmpty()) {
            double sum = reviews.stream().mapToDouble(Review::getRating).sum();
            averageRating = BigDecimal.valueOf(sum / reviews.size());
        }
        
        System.out.println("Calculated average rating: " + averageRating);

        model.addAttribute("photographer", photographer);
        model.addAttribute("totalEvents", upcomingEvents.size() + pastEvents.size());
        model.addAttribute("totalAlbums", albums.size());
        model.addAttribute("totalPhotos", totalPhotos);
        model.addAttribute("averageRating", averageRating);
        
        return "photographer/profile";
    }
    
    // View portfolio images
    @GetMapping("/portfolio")
    public String viewPortfolio(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }
        
        // Split the portfolio string into individual image paths
        String[] portfolioImages = new String[0];
        if (photographer.getPortfolio() != null && !photographer.getPortfolio().isEmpty()) {
            portfolioImages = photographer.getPortfolio().split(",");
        }
        
        model.addAttribute("photographer", photographer);
        model.addAttribute("portfolioImages", portfolioImages);
        return "photographer/portfolio";
    }

    @PostMapping("/profile/update")
    public String updateProfile(HttpSession session,
                                @ModelAttribute Photographer photographer,
                                @RequestParam(value = "portfolioImages", required = false) MultipartFile[] portfolioImages,
                                Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer existingPhotographer = photographerService.getPhotographerByUser(currentUser);
        if (existingPhotographer == null) {
            return "redirect:/auth/login";
        }

        // Update photographer details
        existingPhotographer.setSpecialization(photographer.getSpecialization());
        existingPhotographer.setExperience(photographer.getExperience());
        existingPhotographer.setPortfolio(photographer.getPortfolio());
        existingPhotographer.setEquipment(photographer.getEquipment());
        existingPhotographer.setPricePerHour(photographer.getPricePerHour());
        existingPhotographer.setAvailability(photographer.getAvailability());

        // Handle portfolio image uploads
        if (portfolioImages != null && portfolioImages.length > 0) {
            try {
                StringBuilder portfolioUrls = new StringBuilder();
                String uploadDir = "uploads/portfolio/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                for (int i = 0; i < portfolioImages.length; i++) {
                    MultipartFile file = portfolioImages[i];
                    if (!file.isEmpty()) {
                        // Handle portfolio images
                        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        Path filePath = uploadPath.resolve(fileName);
                        Files.write(filePath, file.getBytes());
                        
                        if (portfolioUrls.length() > 0) {
                            portfolioUrls.append(",");
                        }
                        portfolioUrls.append(uploadDir).append(fileName);
                    }
                }
                
                // Update portfolio with uploaded image URLs
                if (portfolioUrls.length() > 0) {
                    if (existingPhotographer.getPortfolio() != null && !existingPhotographer.getPortfolio().isEmpty()) {
                        existingPhotographer.setPortfolio(existingPhotographer.getPortfolio() + "," + portfolioUrls.toString());
                    } else {
                        existingPhotographer.setPortfolio(portfolioUrls.toString());
                    }
                }
            } catch (IOException e) {
                model.addAttribute("error", "Failed to upload portfolio images: " + e.getMessage());
                model.addAttribute("photographer", existingPhotographer);
                return "photographer/profile";
            }
        }

        photographerService.updatePhotographer(existingPhotographer);

        // Update user details
        User existingUser = existingPhotographer.getUser();
        existingUser.setName(currentUser.getName());
        existingUser.setContactNo(currentUser.getContactNo());
        existingUser.setAddress(currentUser.getAddress());
        userService.updateUser(existingUser);

        model.addAttribute("photographer", existingPhotographer);
        model.addAttribute("success", "Profile updated successfully!");
        return "photographer/profile";
    }

    // Events management
    @GetMapping("/events")
    public String listEvents(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("photographer", photographer);
        model.addAttribute("upcomingEvents", photographerService.getUpcomingEvents(photographer.getPhotographerID()));
        model.addAttribute("pastEvents", photographerService.getPastEventsForAlbumCreation(photographer.getPhotographerID()));

        return "photographer/events";
    }

    // Albums management
    @GetMapping("/albums")
    public String listAlbums(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        List<Album> albums = photographerService.getAlbumsByPhotographer(photographer.getPhotographerID());
        
        // Calculate photo count for each album
        for (Album album : albums) {
            List<Photo> photos = photoService.getPhotosByAlbum(album.getAlbumID());
            album.setPhotoCount(photos.size());
        }

        model.addAttribute("photographer", photographer);
        model.addAttribute("albums", albums);
        model.addAttribute("photoService", photoService);
        // Pass only events assigned to this photographer for album creation
        model.addAttribute("eventsForAlbumCreation", photographerService.getAllEventsForAlbumCreation(photographer.getPhotographerID()));

        return "photographer/albums";
    }

    @PostMapping("/albums/create")
    public String createAlbum(HttpSession session,
                              @ModelAttribute Album album,
                              @RequestParam("eventId") Integer eventId,
                              Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        try {
            // Get the full event with client relationship loaded
            Event albumEvent = eventService.getEventById(eventId);
            if (albumEvent == null) {
                model.addAttribute("error", "Event not found");
                return "redirect:/photographer/albums";
            }
            
            album.setEvent(albumEvent);
            album.setCreatedDate(LocalDateTime.now());

            photographerService.createAlbum(album, photographer.getPhotographerID());
            
            // Notify event attendees that album has been created
            System.out.println("Notifying clients about album creation for event: " + albumEvent.getEventName());
            photographerService.notifyAlbumCreated(albumEvent);
            
            // Also notify all event organizers about the new album
            System.out.println("Notifying all event organizers about album creation for event: " + albumEvent.getEventName());
            photographerService.notifyAllEventOrganizers(albumEvent);

            model.addAttribute("success", "Album created successfully!");
        } catch (Exception e) {
            System.out.println("Error creating album: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to create album: " + e.getMessage());
        }

        return "redirect:/photographer/albums";
    }

    @PostMapping("/albums/{albumId}/delete")
    public String deleteAlbum(@PathVariable Integer albumId,
                              HttpSession session,
                              Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        try {
            // First delete all photos in the album
            List<Photo> photos = photoService.getPhotosByAlbum(albumId);
            for (Photo photo : photos) {
                photoService.deletePhoto(photo.getPhotoID());
            }
            
            // Then delete the album
            albumRepository.deleteById(albumId);
            
            model.addAttribute("success", "Album deleted successfully!");
        } catch (Exception e) {
            System.out.println("Error deleting album: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to delete album: " + e.getMessage());
        }

        return "redirect:/photographer/albums";
    }

    // Photo upload
    @PostMapping("/photos/upload")
    public String uploadPhoto(@RequestParam("albumId") Integer albumId,
                              @RequestParam("photoFile") MultipartFile photoFile,
                              Model model) {
        if (photoFile.isEmpty()) {
            model.addAttribute("error", "Please select a file to upload");
            return "redirect:/photographer/albums";
        }

        try {
            // Save file to server
            String uploadDir = "uploads/photos/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + photoFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, photoFile.getBytes());

            // Save photo info to database
            photographerService.savePhoto(albumId, fileName, uploadDir + fileName);
            
            // Notify event attendees that new photos have been uploaded
            Album album = albumRepository.findById(albumId).orElse(null);
            if (album != null) {
                // Get the full event with client relationship loaded
                Event photoEvent = eventService.getEventById(album.getEvent().getEventID());
                if (photoEvent != null) {
                    System.out.println("Notifying clients about photo upload for event: " + photoEvent.getEventName());
                    photographerService.notifyPhotosUploaded(photoEvent);
                    
                    // Also notify all event organizers about the new photos
                    System.out.println("Notifying all event organizers about photo upload for event: " + photoEvent.getEventName());
                    photographerService.notifyAllEventOrganizers(photoEvent);
                } else {
                    System.out.println("No event found for album ID: " + albumId);
                }
            } else {
                System.out.println("No album found for ID: " + albumId);
            }

            model.addAttribute("success", "Photo uploaded successfully!");
        } catch (IOException e) {
            System.out.println("Error uploading photo: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload photo: " + e.getMessage());
        }

        return "redirect:/photographer/albums";
    }

    // New method to delete photo
    @DeleteMapping("/photos/{photoId}")
    public String deletePhoto(@PathVariable Integer photoId, Model model) {
        try {
            photoService.deletePhoto(photoId);
            model.addAttribute("success", "Photo deleted successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete photo: " + e.getMessage());
        }
        return "redirect:/photographer/albums";
    }

    // New method to view album photos
    @GetMapping("/albums/{albumId}/view")
    public String viewAlbumPhotos(@PathVariable Integer albumId, 
                                  HttpSession session, 
                                  Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        Album album = albumRepository.findById(albumId).orElse(null);
        if (album == null) {
            return "redirect:/photographer/albums";
        }

        List<Photo> photos = photoService.getPhotosByAlbum(albumId);

        model.addAttribute("photographer", photographer);
        model.addAttribute("album", album);
        model.addAttribute("photos", photos);

        return "photographer/album-view";
    }

    // New method to set album cover photo
    @PostMapping("/albums/{albumId}/set-cover")
    public String setAlbumCover(@PathVariable Integer albumId,
                                @RequestParam("photoId") Integer photoId,
                                Model model) {
        try {
            photographerService.setAlbumCoverPhoto(albumId, photoId);
            model.addAttribute("success", "Album cover photo updated successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to set album cover photo: " + e.getMessage());
        }
        return "redirect:/photographer/albums";
    }

    // Financial management - Expenses
    @GetMapping("/expenses")
    public String listExpenses(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        List<Expense> expenses = financialService.getExpensesByPhotographer(photographer.getPhotographerID());
        model.addAttribute("photographer", photographer);
        model.addAttribute("expenses", expenses);

        return "photographer/expenses/list";
    }

    @GetMapping("/expenses/create")
    public String showCreateExpenseForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("photographer", photographer);
        model.addAttribute("expense", new Expense());
        return "photographer/expenses/create";
    }

    @PostMapping("/expenses/create")
    public String createExpense(HttpSession session,
                                @ModelAttribute Expense expense,
                                @RequestParam("expenseDateInput") String expenseDateStr,
                                Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        try {
            expense.setPhotographer(photographer);

            if (expenseDateStr != null && !expenseDateStr.isEmpty()) {
                java.time.LocalDate expenseDate = java.time.LocalDate.parse(expenseDateStr);
                expense.setExpenseDate(expenseDate.atStartOfDay());
            }

            financialService.createExpense(expense);

            model.addAttribute("success", "Expense created successfully!");
            return "redirect:/photographer/expenses";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create expense: " + e.getMessage());
            model.addAttribute("photographer", photographer);
            model.addAttribute("expense", expense);
            return "photographer/expenses/create";
        }
    }

    // Financial dashboard for photographers (restricted view)
    @GetMapping("/financial/dashboard")
    public String showFinancialDashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        // Get financial summary for this photographer
        List<Expense> expenses = financialService.getExpensesByPhotographer(photographer.getPhotographerID());
        List<Booking> bookings = bookingService.getBookingsByPhotographer(photographer.getPhotographerID());
        
        // Calculate financial metrics
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        
        // Calculate total revenue from bookings
        for (Booking booking : bookings) {
            if (booking.getStatus().equals("Confirmed") || booking.getStatus().equals("Completed")) {
                totalRevenue = totalRevenue.add(booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO);
            }
        }
        
        // Calculate total expenses
        for (Expense expense : expenses) {
            totalExpenses = totalExpenses.add(expense.getAmount() != null ? expense.getAmount() : BigDecimal.ZERO);
        }
        
        // Calculate profit
        totalProfit = totalRevenue.subtract(totalExpenses);

        model.addAttribute("photographer", photographer);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("totalProfit", totalProfit);
        model.addAttribute("bookings", bookings);
        model.addAttribute("expenses", expenses);

        return "photographer/financial/dashboard";
    }
    
    // Generate financial report for photographer
    @GetMapping("/financial/report")
    public String generateFinancialReport(HttpSession session, 
                                        @RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate,
                                        Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        // Parse dates if provided
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        // Get financial data for the period
        List<Expense> allExpenses = financialService.getExpensesByPhotographer(photographer.getPhotographerID());
        List<Expense> expenses = allExpenses.stream()
            .filter(expense -> expense.getExpenseDate() != null && 
                     !expense.getExpenseDate().toLocalDate().isBefore(start) && 
                     !expense.getExpenseDate().toLocalDate().isAfter(end))
            .collect(Collectors.toList());
            
        List<Booking> allBookings = bookingService.getBookingsByPhotographer(photographer.getPhotographerID());
        List<Booking> bookings = allBookings.stream()
            .filter(booking -> booking.getBookingDate() != null && 
                     !booking.getBookingDate().toLocalDate().isBefore(start) && 
                     !booking.getBookingDate().toLocalDate().isAfter(end))
            .collect(Collectors.toList());
        
        // Calculate financial metrics
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        
        // Calculate total revenue from bookings
        for (Booking booking : bookings) {
            if (booking.getStatus().equals("Confirmed") || booking.getStatus().equals("Completed")) {
                totalRevenue = totalRevenue.add(booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO);
            }
        }
        
        // Calculate total expenses
        for (Expense expense : expenses) {
            totalExpenses = totalExpenses.add(expense.getAmount() != null ? expense.getAmount() : BigDecimal.ZERO);
        }
        
        // Calculate profit
        totalProfit = totalRevenue.subtract(totalExpenses);

        model.addAttribute("photographer", photographer);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("totalProfit", totalProfit);
        model.addAttribute("bookings", bookings);
        model.addAttribute("expenses", expenses);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);

        return "photographer/financial/report";
    }
    
    // Create album from events page
    @PostMapping("/events/{eventId}/create-album")
    public String createAlbumFromEvent(HttpSession session,
                              @PathVariable Integer eventId,
                              Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        try {
            // Create a new album for this event
            Album album = new Album();
            album.setAlbumName("Album for " + eventId); // Default name, can be changed later
            album.setDescription("Photos from event"); // Default description
            
            // Get the full event
            Event albumEvent = eventService.getEventById(eventId);
            if (albumEvent == null) {
                model.addAttribute("error", "Event not found");
                return "redirect:/photographer/events";
            }
            
            album.setEvent(albumEvent);
            album.setCreatedDate(LocalDateTime.now());

            photographerService.createAlbum(album, photographer.getPhotographerID());
            
            // Notify event attendees that album has been created
            System.out.println("Notifying clients about album creation for event: " + albumEvent.getEventName());
            photographerService.notifyAlbumCreated(albumEvent);
            
            // Also notify all event organizers about the new album
            System.out.println("Notifying all event organizers about album creation for event: " + albumEvent.getEventName());
            photographerService.notifyAllEventOrganizers(albumEvent);

            model.addAttribute("success", "Album created successfully!");
        } catch (Exception e) {
            System.out.println("Error creating album: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to create album: " + e.getMessage());
        }

        return "redirect:/photographer/events";
    }

    // Manual trigger for album initialization (for debugging)
    @GetMapping("/initialize-albums")
    public String initializeAlbums(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }
        
        // Manually trigger album initialization
        photographerService.initializeAlbums();
        
        model.addAttribute("photographer", photographer);
        model.addAttribute("message", "Album initialization completed. Check console for details.");
        return "photographer/dashboard";
    }
    
    // Check album status (for debugging)
    @GetMapping("/check-albums")
    public String checkAlbums(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !UserTypeChecker.isPhotographer(currentUser)) {
            return "redirect:/auth/login";
        }
        
        Photographer photographer = photographerService.getPhotographerByUser(currentUser);
        if (photographer == null) {
            return "redirect:/auth/login";
        }
        
        // Check album status
        albumStatusChecker.checkAlbumStatus();
        
        model.addAttribute("photographer", photographer);
        model.addAttribute("message", "Album status check completed. Check console for details.");
        return "photographer/dashboard";
    }
}