package com.eventphotography.management.repository;

import com.eventphotography.management.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {
    List<Equipment> findByPhotographer_PhotographerID(Integer photographerID);
    List<Equipment> findByStatus(String status);
}