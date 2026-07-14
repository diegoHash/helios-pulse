package com.helios.platform.pulse.repositories;

import com.helios.platform.pulse.entities.OperatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOperator extends JpaRepository<OperatorEntity, Long> {
    List<OperatorEntity> findByActiveTrue();
}
