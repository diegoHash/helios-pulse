package com.helios.platform.pulse.controller;

import com.helios.platform.pulse.entities.OperatorEntity;
import com.helios.platform.pulse.service.OperatorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/helios/pulse/operators")
public class OperatorController {

    private final OperatorService operatorService;

    public OperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @GetMapping
    public List<OperatorEntity> getAll() {
        return operatorService.getAllOperators();
    }

    @GetMapping("/active")
    public List<OperatorEntity> getActive() {
        return operatorService.getActiveOperators();
    }

    @PostMapping
    public OperatorEntity create(@RequestBody OperatorEntity operator) {
        return operatorService.saveOperator(operator);
    }

    @PutMapping("/{id}")
    public OperatorEntity update(@PathVariable("id") Long id, @RequestBody OperatorEntity operator) {
        operator.setId(id);
        return operatorService.saveOperator(operator);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        operatorService.deleteOperator(id);
    }
}
