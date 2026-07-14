package com.helios.platform.pulse.repositories;

import com.helios.platform.pulse.entities.CorrectionRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICorrectionRequestRepository extends JpaRepository<CorrectionRequestEntity, Long> {
    List<CorrectionRequestEntity> findAllByOrderByRequestDateDesc();
    List<CorrectionRequestEntity> findByStatusOrderByRequestDateDesc(String status);
}
