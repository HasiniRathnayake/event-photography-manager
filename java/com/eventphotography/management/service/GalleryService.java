package com.eventphotography.management.service;

import com.eventphotography.management.entity.Gallery;
import com.eventphotography.management.repository.GalleryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class GalleryService {

    @Autowired
    private GalleryRepository galleryRepository;

    public List<Gallery> getAllGalleries() {
        return galleryRepository.findAll();
    }

    public List<Gallery> getGalleriesByPhotographer(Integer photographerId) {
        return galleryRepository.findByPhotographer_PhotographerID(photographerId);
    }

    public List<Gallery> getPublicGalleries() {
        return galleryRepository.findByIsPublicTrue();
    }

    public Gallery getGalleryById(Integer galleryId) {
        return galleryRepository.findById(galleryId).orElse(null);
    }

    @Transactional
    public Gallery createGallery(Gallery gallery) {
        return galleryRepository.save(gallery);
    }

    @Transactional
    public Gallery updateGallery(Gallery gallery) {
        return galleryRepository.save(gallery);
    }

    @Transactional
    public boolean deleteGallery(Integer galleryId) {
        if (galleryRepository.existsById(galleryId)) {
            galleryRepository.deleteById(galleryId);
            return true;
        }
        return false;
    }
}