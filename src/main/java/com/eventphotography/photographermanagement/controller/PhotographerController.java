package com.eventphotography.photographermanagement.controller;

import com.eventphotography.photographermanagement.model.Album;
import com.eventphotography.photographermanagement.model.Event;
import com.eventphotography.photographermanagement.model.Photographer;
import com.eventphotography.photographermanagement.model.Review;
import com.eventphotography.photographermanagement.service.PhotographerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/photographer")
public class PhotographerController {

    private final PhotographerService photographerService;
    private static final String UPLOAD_DIR = "uploads/";

    public PhotographerController(PhotographerService photographerService) {
        this.photographerService = photographerService;
    }

    @GetMapping("/{photographerId}/dashboard")
    public String getPhotographerDashboard(@PathVariable int photographerId, Model model) {
        Photographer photographer = photographerService.getPhotographerDetails(photographerId);
        List<Event> upcomingEvents = photographerService.getUpcomingEvents(photographerId);
        List<Event> pastEvents = photographerService.getPastEventsForAlbumCreation(photographerId);
        List<Review> recentReviews = photographerService.getRecentReviews(photographerId);
        List<Album> albums = photographerService.getAlbumsByPhotographer(photographerId);

        model.addAttribute("photographer", photographer);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("pastEvents", pastEvents);
        model.addAttribute("recentReviews", recentReviews);
        model.addAttribute("albums", albums);
        model.addAttribute("newAlbum", new Album());

        return "photographer-dashboard";
    }

    @PostMapping("/album/create")
    public String createAlbum(@ModelAttribute Album album, @RequestParam("photographerId") int photographerId) {
        photographerService.createAlbum(album, photographerId);
        return "redirect:/photographer/" + photographerId + "/dashboard";
    }

    @PostMapping("/photo/upload")
    public String uploadPhotos(@RequestParam("files") MultipartFile[] files, @RequestParam("albumId") int albumId, @RequestParam("photographerId") int photographerId) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR + "albums/" + albumId);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                Path filePath = uploadPath.resolve(file.getOriginalFilename());
                Files.copy(file.getInputStream(), filePath);

                // Save photo metadata to the database
                photographerService.savePhoto(albumId, file.getOriginalFilename(), filePath.toString());
            }
        } catch (IOException e) {
            // Add proper error handling
            e.printStackTrace();
        }
        return "redirect:/photographer/" + photographerId + "/dashboard";
    }
}
