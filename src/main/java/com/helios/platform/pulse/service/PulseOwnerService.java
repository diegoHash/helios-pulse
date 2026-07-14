package com.helios.platform.pulse.service;

import com.helios.platform.pulse.entities.PropietarioModel;
import com.helios.platform.pulse.repositories.IPulseOwnerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class PulseOwnerService {

    private final IPulseOwnerRepository ownerRepo;
    private final NotificationService notificationService;

    public PulseOwnerService(IPulseOwnerRepository ownerRepo, NotificationService notificationService) {
        this.ownerRepo = ownerRepo;
        this.notificationService = notificationService;
    }

    public ResponseEntity<String> reset(){
        ownerRepo.resetAll();
        return ResponseEntity.ok("Success");
    }

    public List<PropietarioModel> getOwners() {
        return ownerRepo.findAll();
    }

    public HashMap<String, Long> getPropertiesID() {
        return ownerRepo.findProperties();
    }

    public ResponseEntity<String> update(long id, int value) {
        int updatedRows = ownerRepo.deductCupo(id, value);

        if (updatedRows == 0) {
            notificationService.sendNotification("We tried to find the user with the ID: " + id + " but no properties were found");
            return ResponseEntity.notFound().build();
        }

        notificationService.sendNotification("Updated successfully property ID: " + id + " deducting " + value + " bracelets.");
        return ResponseEntity.ok("UPDATE SATISFIED SUCCESSFULLY!");
    }

    public PropietarioModel getOwnerById(long id) {
        return ownerRepo.findById(id).orElse(null);
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return phone;
        }

        // Remove everything except + and digits
        String cleanPhone = phone.replaceAll("[^\\d+]", "");

        // If they wrote 580424... instead of +58424... (length 13)
        if (cleanPhone.startsWith("580") && cleanPhone.length() == 13) {
            cleanPhone = "+58" + cleanPhone.substring(3);
        } else if (cleanPhone.startsWith("+580") && cleanPhone.length() == 14) {
            cleanPhone = "+58" + cleanPhone.substring(4);
        }

        if (cleanPhone.startsWith("+58")) {
            return cleanPhone;
        } else if (cleanPhone.startsWith("58") && cleanPhone.length() == 12) {
            return "+" + cleanPhone;
        } else if (cleanPhone.startsWith("0")) {
            return "+58" + cleanPhone.substring(1);
        } else if (!cleanPhone.startsWith("+") && cleanPhone.length() == 10) {
            return "+58" + cleanPhone;
        }

        return cleanPhone;
    }

    public ResponseEntity<String> updatePhone(long id, String telefono) {
        String formattedPhone = formatPhoneNumber(telefono);
        int updatedRows = ownerRepo.updateTelefono(id, formattedPhone);
        if (updatedRows == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Phone updated successfully");
    }
}
