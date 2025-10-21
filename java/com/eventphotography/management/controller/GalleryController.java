package com.eventphotography.management.controller;

import com.eventphotography.management.entity.Gallery;
import com.eventphotography.management.entity.User;
import com.eventphotography.management.service.GalleryService;
import com.eventphotography.management.service.PhotographerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/galleries")
public class GalleryController {

    @Autowired
    private GalleryService galleryService;

    @Autowired
    private PhotographerService photographerService;

    @GetMapping
    public String listGalleries(@SessionAttribute("user") User user, Model model) {
        // Only photographers can manage galleries
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("galleryList", galleryService.getGalleriesByPhotographer(photographer.getPhotographerID()));
        model.addAttribute("photographer", photographer);
        return "gallery/list";
    }

    @GetMapping("/create")
    public String showCreateForm(@SessionAttribute("user") User user, Model model) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("gallery", new Gallery());
        model.addAttribute("photographer", photographer);
        return "gallery/create";
    }

    @PostMapping("/create")
    public String createGallery(@SessionAttribute("user") User user,
                                @ModelAttribute Gallery gallery,
                                Model model) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        gallery.setPhotographer(photographer);
        galleryService.createGallery(gallery);
        return "redirect:/galleries";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@SessionAttribute("user") User user,
                               @PathVariable Integer id,
                               Model model) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        Gallery gallery = galleryService.getGalleryById(id);
        if (gallery == null || !gallery.getPhotographer().getPhotographerID().equals(photographer.getPhotographerID())) {
            return "redirect:/galleries";
        }

        model.addAttribute("gallery", gallery);
        model.addAttribute("photographer", photographer);
        return "gallery/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateGallery(@SessionAttribute("user") User user,
                                @PathVariable Integer id,
                                @ModelAttribute Gallery gallery,
                                Model model) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        Gallery existingGallery = galleryService.getGalleryById(id);
        if (existingGallery == null || !existingGallery.getPhotographer().getPhotographerID().equals(photographer.getPhotographerID())) {
            return "redirect:/galleries";
        }

        existingGallery.setTitle(gallery.getTitle());
        existingGallery.setDescription(gallery.getDescription());
        existingGallery.setIsPublic(gallery.getIsPublic());
        existingGallery.setStatus(gallery.getStatus());

        galleryService.updateGallery(existingGallery);
        return "redirect:/galleries";
    }

    @PostMapping("/delete/{id}")
    public String deleteGallery(@SessionAttribute("user") User user,
                                @PathVariable Integer id) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        Gallery gallery = galleryService.getGalleryById(id);
        if (gallery != null && gallery.getPhotographer().getPhotographerID().equals(photographer.getPhotographerID())) {
            galleryService.deleteGallery(id);
        }

        return "redirect:/galleries";
    }
}