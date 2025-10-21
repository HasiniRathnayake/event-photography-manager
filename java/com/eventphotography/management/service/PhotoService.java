package com.eventphotography.management.service;

import com.eventphotography.management.entity.Photo;
import com.eventphotography.management.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Value("${photo.storage.base-dir}")
    private String baseDir;

    public List<Photo> getPhotosByEvent(Integer eventId) {
        return photoRepository.findByEventId(eventId);
    }

    public List<Photo> getPhotosByAlbum(Integer albumId) {
        List<Photo> photos = photoRepository.findByAlbumId(albumId);
        System.out.println("Found " + photos.size() + " photos for album ID: " + albumId);
        return photos;
    }

    public Photo getPhotoById(Integer photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
    }

    public Path getAbsolutePath(Photo photo) {
        return Paths.get(baseDir).resolve(photo.getFilePath());
    }

    public Path getAbsoluteThumbnailPath(Photo photo) {
        return Paths.get(baseDir).resolve(photo.getThumbnailPath());
    }

    public Photo savePhoto(Photo photo) {
        return photoRepository.save(photo);
    }

    public void deletePhoto(Integer photoId) {
        photoRepository.deleteById(photoId);
    }
}
