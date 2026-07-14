package com.helios.platform.pulse.controller;

import com.helios.platform.pulse.entities.OperatorEntity;
import com.helios.platform.pulse.service.OperatorService;
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
class OperatorControllerTest {

    @Mock
    private OperatorService operatorService;

    @InjectMocks
    private OperatorController operatorController;

    @Test
    void testGetAll() {
        OperatorEntity op = new OperatorEntity();
        op.setName("Test");
        when(operatorService.getAllOperators()).thenReturn(Collections.singletonList(op));

        List<OperatorEntity> result = operatorController.getAll();

        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getName());
    }

    @Test
    void testGetActive() {
        OperatorEntity op = new OperatorEntity();
        op.setName("ActiveTest");
        when(operatorService.getActiveOperators()).thenReturn(Collections.singletonList(op));

        List<OperatorEntity> result = operatorController.getActive();

        assertEquals(1, result.size());
        assertEquals("ActiveTest", result.get(0).getName());
    }

    @Test
    void testCreate() {
        OperatorEntity op = new OperatorEntity();
        op.setName("NewTest");
        when(operatorService.saveOperator(any(OperatorEntity.class))).thenReturn(op);

        OperatorEntity result = operatorController.create(new OperatorEntity());

        assertEquals("NewTest", result.getName());
    }

    @Test
    void testUpdate() {
        OperatorEntity op = new OperatorEntity();
        op.setName("UpdatedTest");
        when(operatorService.saveOperator(any(OperatorEntity.class))).thenReturn(op);

        OperatorEntity result = operatorController.update(1L, new OperatorEntity());

        assertEquals("UpdatedTest", result.getName());
    }

    @Test
    void testDelete() {
        doNothing().when(operatorService).deleteOperator(1L);

        operatorController.delete(1L);

        verify(operatorService, times(1)).deleteOperator(1L);
    }
}
