package com.helios.platform.pulse.controller;

import com.helios.platform.pulse.entities.PropietarioModel;
import com.helios.platform.pulse.service.PulseOwnerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/helios/pulse")
public class OwnerController {

    private final PulseOwnerService ownerService;
    private final com.helios.platform.pulse.service.BraceletServiceV2 braceletServiceV2;

    public OwnerController(PulseOwnerService ownerService, com.helios.platform.pulse.service.BraceletServiceV2 braceletServiceV2) {
        this.ownerService = ownerService;
        this.braceletServiceV2 = braceletServiceV2;
    }

    @GetMapping("/owners")
    public List<PropietarioModel> getOwnersInfo() {
        return ownerService.getOwners();
    }

    @GetMapping("/owners/reset-quotas")
    public org.springframework.http.ResponseEntity<String> resetQuotas() {
        return ownerService.reset();
    }

    @GetMapping("/currentserial")
    public Integer getCurrentSerial() {
        return braceletServiceV2.getMaxSerial();
    }

    @GetMapping("/remaining")
    public Integer getRemainingBracelets() {
        return braceletServiceV2.getRemaining();
    }

    @org.springframework.web.bind.annotation.PutMapping("/owners/{id}/phone")
    public org.springframework.http.ResponseEntity<String> updatePhone(
            @org.springframework.web.bind.annotation.PathVariable("id") long id,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> body) {
        String telefono = body.get("telefono");
        return ownerService.updatePhone(id, telefono);
    }
}
