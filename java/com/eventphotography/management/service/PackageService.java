package com.eventphotography.management.service;

import com.eventphotography.management.entity.Package;
import com.eventphotography.management.repository.PackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PackageService {

    @Autowired
    private PackageRepository packageRepository;

    public List<Package> getAllPackages() {
        return packageRepository.findAll();
    }

    public List<Package> getPackagesByPhotographer(Integer photographerId) {
        return packageRepository.findByPhotographer_PhotographerID(photographerId);
    }

    public List<Package> getPackagesByStatus(String status) {
        return packageRepository.findByStatus(status);
    }

    public Package getPackageById(Integer packageId) {
        return packageRepository.findById(packageId).orElse(null);
    }

    @Transactional
    public Package createPackage(Package pkg) {
        return packageRepository.save(pkg);
    }

    @Transactional
    public Package updatePackage(Package pkg) {
        return packageRepository.save(pkg);
    }

    @Transactional
    public boolean deletePackage(Integer packageId) {
        if (packageRepository.existsById(packageId)) {
            packageRepository.deleteById(packageId);
            return true;
        }
        return false;
    }
}