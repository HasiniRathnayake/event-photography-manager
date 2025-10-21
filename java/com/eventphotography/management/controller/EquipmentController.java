package com.eventphotography.management.controller;

import com.eventphotography.management.entity.Equipment;
import com.eventphotography.management.entity.User;
import com.eventphotography.management.service.EquipmentService;
import com.eventphotography.management.service.PhotographerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private PhotographerService photographerService;

    @GetMapping
    public String listEquipment(@SessionAttribute("user") User user, Model model) {
        // Only photographers can manage equipment
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("equipmentList", equipmentService.getEquipmentByPhotographer(photographer.getPhotographerID()));
        model.addAttribute("photographer", photographer);
        return "equipment/list";
    }

    @GetMapping("/create")
    public String showCreateForm(@SessionAttribute("user") User user, Model model) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("equipment", new Equipment());
        model.addAttribute("photographer", photographer);
        return "equipment/create";
    }

    @PostMapping("/create")
    public String createEquipment(@SessionAttribute("user") User user,
                                  @ModelAttribute Equipment equipment,
                                  Model model) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        equipment.setPhotographer(photographer);
        equipmentService.createEquipment(equipment);
        return "redirect:/equipment";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@SessionAttribute("user") User user,
                               @PathVariable Integer id,
                               Model model) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        Equipment equipment = equipmentService.getEquipmentById(id);
        if (equipment == null || !equipment.getPhotographer().getPhotographerID().equals(photographer.getPhotographerID())) {
            return "redirect:/equipment";
        }

        model.addAttribute("equipment", equipment);
        model.addAttribute("photographer", photographer);
        return "equipment/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateEquipment(@SessionAttribute("user") User user,
                                  @PathVariable Integer id,
                                  @ModelAttribute Equipment equipment,
                                  Model model) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        Equipment existingEquipment = equipmentService.getEquipmentById(id);
        if (existingEquipment == null || !existingEquipment.getPhotographer().getPhotographerID().equals(photographer.getPhotographerID())) {
            return "redirect:/equipment";
        }

        existingEquipment.setName(equipment.getName());
        existingEquipment.setDescription(equipment.getDescription());
        existingEquipment.setCategory(equipment.getCategory());
        existingEquipment.setValue(equipment.getValue());
        existingEquipment.setStatus(equipment.getStatus());
        existingEquipment.setCondition(equipment.getCondition());
        existingEquipment.setSerialNumber(equipment.getSerialNumber());

        equipmentService.updateEquipment(existingEquipment);
        return "redirect:/equipment";
    }

    @PostMapping("/delete/{id}")
    public String deleteEquipment(@SessionAttribute("user") User user,
                                  @PathVariable Integer id) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        Equipment equipment = equipmentService.getEquipmentById(id);
        if (equipment != null && equipment.getPhotographer().getPhotographerID().equals(photographer.getPhotographerID())) {
            equipmentService.deleteEquipment(id);
        }

        return "redirect:/equipment";
    }
}