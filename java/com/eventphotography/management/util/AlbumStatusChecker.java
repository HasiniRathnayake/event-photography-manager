package com.eventphotography.management.util;

import com.eventphotography.management.entity.Album;
import com.eventphotography.management.repository.AlbumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AlbumStatusChecker {
    
    @Autowired
    private AlbumRepository albumRepository;
    
    public void checkAlbumStatus() {
        List<Album> allAlbums = albumRepository.findAll();
        System.out.println("=== Album Status Check ===");
        System.out.println("Total albums: " + allAlbums.size());
        
        int publicCount = 0;
        int privateCount = 0;
        int nullCount = 0;
        
        for (Album album : allAlbums) {
            if (album.getIsPublic() == null) {
                nullCount++;
                System.out.println("Album ID " + album.getAlbumID() + " (" + album.getAlbumName() + ") - isPublic: NULL");
            } else if (album.getIsPublic()) {
                publicCount++;
                System.out.println("Album ID " + album.getAlbumID() + " (" + album.getAlbumName() + ") - isPublic: TRUE");
            } else {
                privateCount++;
                System.out.println("Album ID " + album.getAlbumID() + " (" + album.getAlbumName() + ") - isPublic: FALSE");
            }
        }
        
        System.out.println("Public albums: " + publicCount);
        System.out.println("Private albums: " + privateCount);
        System.out.println("NULL albums: " + nullCount);
        System.out.println("=== End Album Status Check ===");
    }
}