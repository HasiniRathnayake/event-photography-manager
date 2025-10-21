package com.eventphotography.management.service;

import com.eventphotography.management.entity.Equipment;
import com.eventphotography.management.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    public List<Equipment> getEquipmentByPhotographer(Integer photographerId) {
        return equipmentRepository.findByPhotographer_PhotographerID(photographerId);
    }

    public List<Equipment> getEquipmentByStatus(String status) {
        return equipmentRepository.findByStatus(status);
    }

    public Equipment getEquipmentById(Integer equipmentId) {
        return equipmentRepository.findById(equipmentId).orElse(null);
    }

    @Transactional
    public Equipment createEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public Equipment updateEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public boolean deleteEquipment(Integer equipmentId) {
        if (equipmentRepository.existsById(equipmentId)) {
            equipmentRepository.deleteById(equipmentId);
            return true;
        }
        return false;
    }
}