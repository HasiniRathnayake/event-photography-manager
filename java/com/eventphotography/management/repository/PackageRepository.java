package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, Integer> {
    List<Package> findByPhotographer_PhotographerID(Integer photographerID);
    List<Package> findByStatus(String status);
}