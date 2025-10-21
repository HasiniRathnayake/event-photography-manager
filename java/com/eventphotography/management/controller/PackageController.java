package com.eventphotography.management.controller;

import com.eventphotography.management.entity.Package;
import com.eventphotography.management.entity.User;
import com.eventphotography.management.service.PackageService;
import com.eventphotography.management.service.PhotographerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/packages")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @Autowired
    private PhotographerService photographerService;

    @GetMapping
    public String listPackages(@SessionAttribute("user") User user, Model model) {
        // Only photographers can manage packages
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("packageList", packageService.getPackagesByPhotographer(photographer.getPhotographerID()));
        model.addAttribute("photographer", photographer);
        return "package/list";
    }

    @GetMapping("/create")
    public String showCreateForm(@SessionAttribute("user") User user, Model model) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("pkg", new Package());
        model.addAttribute("photographer", photographer);
        return "package/create";
    }

    @PostMapping("/create")
    public String createPackage(@SessionAttribute("user") User user,
                                @ModelAttribute("pkg") Package pkg,
                                Model model) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        pkg.setPhotographer(photographer);
        packageService.createPackage(pkg);
        return "redirect:/packages";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@SessionAttribute("user") User user,
                               @PathVariable Integer id,
                               Model model) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        Package pkg = packageService.getPackageById(id);
        if (pkg == null || !pkg.getPhotographer().getPhotographerID().equals(photographer.getPhotographerID())) {
            return "redirect:/packages";
        }

        model.addAttribute("pkg", pkg);
        model.addAttribute("photographer", photographer);
        return "package/edit";
    }

    @PostMapping("/edit/{id}")
    public String updatePackage(@SessionAttribute("user") User user,
                                @PathVariable Integer id,
                                @ModelAttribute("pkg") Package pkg,
                                Model model) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        Package existingPackage = packageService.getPackageById(id);
        if (existingPackage == null || !existingPackage.getPhotographer().getPhotographerID().equals(photographer.getPhotographerID())) {
            return "redirect:/packages";
        }

        existingPackage.setName(pkg.getName());
        existingPackage.setDescription(pkg.getDescription());
        existingPackage.setDurationHours(pkg.getDurationHours());
        existingPackage.setPrice(pkg.getPrice());
        existingPackage.setNumberOfPhotos(pkg.getNumberOfPhotos());
        existingPackage.setIsEditingIncluded(pkg.getIsEditingIncluded());
        existingPackage.setEditingTimeDays(pkg.getEditingTimeDays());
        existingPackage.setStatus(pkg.getStatus());
        existingPackage.setPackageType(pkg.getPackageType());

        packageService.updatePackage(existingPackage);
        return "redirect:/packages";
    }

    @PostMapping("/delete/{id}")
    public String deletePackage(@SessionAttribute("user") User user,
                                @PathVariable Integer id) {
        var photographer = photographerService.getPhotographerByUser(user);
        if (photographer == null) {
            return "redirect:/auth/login";
        }

        Package pkg = packageService.getPackageById(id);
        if (pkg != null && pkg.getPhotographer().getPhotographerID().equals(photographer.getPhotographerID())) {
            packageService.deletePackage(id);
        }

        return "redirect:/packages";
    }
}