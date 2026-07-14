package com.helios.platform.pulse.service;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.CorrectionRequestDto;
import com.helios.platform.pulse.entities.CorrectionRequestEntity;
import com.helios.platform.pulse.entities.UserEntity;
import com.helios.platform.pulse.repositories.ICorrectionRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CorrectionRequestService {

    private final ICorrectionRequestRepository repository;
    private final EmailService emailService;

    public void createRequest(CorrectionRequestDto dto, UserEntity currentUser, String deviceInfo, String ipAddress) {
        CorrectionRequestEntity entity = new CorrectionRequestEntity();
        entity.setTransactionId(dto.getTransactionId());
        entity.setRequesterId(currentUser.getId());
        entity.setRequesterName(currentUser.getUsername());
        entity.setFieldToModify(dto.getFieldToModify());
        entity.setMessage(dto.getMessage());
        entity.setStatus("PENDING");
        entity.setDeviceInfo(deviceInfo);
        entity.setIpAddress(ipAddress);
        entity.setRequestDate(LocalDateTime.now());

        repository.save(entity);

        // Enviar email
        emailService.sendCorrectionRequestEmail(
                "diegosumozar@gmail.com",
                currentUser.getUsername(),
                dto.getTransactionId(),
                dto.getFieldToModify(),
                dto.getMessage(),
                deviceInfo
        );
    }

    public List<CorrectionRequestEntity> getPendingRequests() {
        return repository.findByStatusOrderByRequestDateDesc("PENDING");
    }

    public List<CorrectionRequestEntity> getAllRequests() {
        return repository.findAllByOrderByRequestDateDesc();
    }

    public void updateStatus(Long requestId, String status) {
        Optional<CorrectionRequestEntity> opt = repository.findById(requestId);
        if (opt.isPresent()) {
            CorrectionRequestEntity entity = opt.get();
            entity.setStatus(status);
            entity.setResolutionDate(LocalDateTime.now());
            repository.save(entity);
        }
    }
}
