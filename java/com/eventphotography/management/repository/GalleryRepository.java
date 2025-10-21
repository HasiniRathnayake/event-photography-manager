package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Gallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Integer> {
    List<Gallery> findByPhotographer_PhotographerID(Integer photographerID);
    List<Gallery> findByIsPublicTrue();
}