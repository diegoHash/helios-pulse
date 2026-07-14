package com.helios.platform.pulse.service;

import com.helios.platform.pulse.entities.OperatorEntity;
import com.helios.platform.pulse.repositories.IOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperatorServiceTest {

    @Mock
    private IOperator operatorRepository;

    @InjectMocks
    private OperatorService operatorService;

    @Test
    void testGetAllOperators() {
        OperatorEntity op = new OperatorEntity();
        op.setName("John");
        when(operatorRepository.findAll()).thenReturn(Collections.singletonList(op));

        List<OperatorEntity> result = operatorService.getAllOperators();
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getName());
    }

    @Test
    void testGetActiveOperators() {
        OperatorEntity op = new OperatorEntity();
        op.setName("ActiveJohn");
        op.setActive(true);
        when(operatorRepository.findByActiveTrue()).thenReturn(Collections.singletonList(op));

        List<OperatorEntity> result = operatorService.getActiveOperators();
        assertEquals(1, result.size());
        assertEquals("ActiveJohn", result.get(0).getName());
    }

    @Test
    void testSaveOperator() {
        OperatorEntity op = new OperatorEntity();
        op.setName("NewJohn");
        when(operatorRepository.save(any(OperatorEntity.class))).thenReturn(op);

        OperatorEntity result = operatorService.saveOperator(op);
        assertEquals("NewJohn", result.getName());
    }

    @Test
    void testDeleteOperator() {
        doNothing().when(operatorRepository).deleteById(1L);
        operatorService.deleteOperator(1L);
        verify(operatorRepository, times(1)).deleteById(1L);
    }
}
