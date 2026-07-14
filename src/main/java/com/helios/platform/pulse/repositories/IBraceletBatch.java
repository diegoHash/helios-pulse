package com.helios.platform.pulse.repositories;

import com.helios.platform.pulse.entities.BraceletBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBraceletBatch extends JpaRepository<BraceletBatchEntity, Long> {
}
