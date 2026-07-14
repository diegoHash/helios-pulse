package com.helios.platform.pulse.service;

import com.helios.platform.pulse.entities.OperatorEntity;
import com.helios.platform.pulse.repositories.IOperator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperatorService {

    private final IOperator operatorRepository;

    public OperatorService(IOperator operatorRepository) {
        this.operatorRepository = operatorRepository;
    }

    public List<OperatorEntity> getAllOperators() {
        return operatorRepository.findAll();
    }

    public List<OperatorEntity> getActiveOperators() {
        return operatorRepository.findByActiveTrue();
    }

    public OperatorEntity saveOperator(OperatorEntity operator) {
        return operatorRepository.save(operator);
    }

    public void deleteOperator(Long id) {
        operatorRepository.deleteById(id);
    }
}
