package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Integer> {

    @Query("SELECT p FROM Photo p WHERE p.album.albumID = :albumId")
    List<Photo> findByAlbumId(@Param("albumId") Integer albumId);

    @Query("SELECT p FROM Photo p WHERE p.album.event.eventID = :eventId")
    List<Photo> findByEventId(@Param("eventId") Integer eventId);
}
