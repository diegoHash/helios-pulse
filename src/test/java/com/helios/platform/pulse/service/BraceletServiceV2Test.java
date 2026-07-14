package com.helios.platform.pulse.service;

import com.helios.platform.pulse.dto.BraceletBatchDTO;
import com.helios.platform.pulse.dto.PaymentBatchDTO;
import com.helios.platform.pulse.dto.TransactionBatchDTO;
import com.helios.platform.pulse.entities.BraceletBatchEntity;
import com.helios.platform.pulse.entities.LoteEntity;
import com.helios.platform.pulse.entities.PropietarioModel;
import com.helios.platform.pulse.entities.TransactionEntityV2;
import com.helios.platform.pulse.mapper.TransactionMapper;
import com.helios.platform.pulse.repositories.ILoteRepo;
import com.helios.platform.pulse.repositories.ITransactionV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BraceletServiceV2Test {

    @Mock
    private ITransactionV2 transactionV2Repository;

    @Mock
    private PulseOwnerService cbtOwnerService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private ILoteRepo loteRepo;

    @Mock
    private WhatsAppService whatsappService;

    @InjectMocks
    private BraceletServiceV2 braceletService;

    private HashMap<String, Long> validOwners;

    @BeforeEach
    void setUp() {
        validOwners = new HashMap<>();
        validOwners.put("V100", 1L);
        validOwners.put("V200", 2L);
    }

    @Test
    void testSaveTransactionsV2_NullOrEmptyList() {
        ResponseEntity<?> response = braceletService.saveTransactionsV2(null);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("NO TRANSACTIONS PROVIDED", response.getBody());

        response = braceletService.saveTransactionsV2(new ArrayList<>());
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testSaveTransactionsV2_OwnersServiceFails() {
        when(cbtOwnerService.getPropertiesID()).thenReturn(null);
        List<TransactionBatchDTO> dtoList = Collections.singletonList(new TransactionBatchDTO());

        ResponseEntity<?> response = braceletService.saveTransactionsV2(dtoList);
        assertEquals(500, response.getStatusCode().value());
        assertEquals("SERVICE UNAVAILABLE: Unable to load properties data.", response.getBody());
    }

    @Test
    void testSaveTransactionsV2_BlankProperty() {
        when(cbtOwnerService.getPropertiesID()).thenReturn(validOwners);
        TransactionBatchDTO dto = new TransactionBatchDTO();
        dto.setProperty("");

        ResponseEntity<?> response = braceletService.saveTransactionsV2(Collections.singletonList(dto));
        assertEquals(400, response.getStatusCode().value());
        assertEquals("NOT ALLOWED EMPTY OR BLANK PROPERTY FIELD", response.getBody());
    }

    @Test
    void testSaveTransactionsV2_InvalidProperty() {
        when(cbtOwnerService.getPropertiesID()).thenReturn(validOwners);
        TransactionBatchDTO dto = new TransactionBatchDTO();
        dto.setProperty("INVALID");

        ResponseEntity<?> response = braceletService.saveTransactionsV2(Collections.singletonList(dto));
        assertEquals(400, response.getStatusCode().value());
        assertEquals("PROPERTY MUST BE VALID", response.getBody());
    }

    @Test
    void testSaveTransactionsV2_InvalidType() {
        when(cbtOwnerService.getPropertiesID()).thenReturn(validOwners);
        TransactionBatchDTO dto = new TransactionBatchDTO();
        dto.setProperty("V100");
        dto.setType("UNKNOWN_TYPE");

        ResponseEntity<?> response = braceletService.saveTransactionsV2(Collections.singletonList(dto));
        assertEquals(400, response.getStatusCode().value());
        assertEquals("NOT ALLOWED TYPE OF BRACELET", response.getBody());
    }

    @Test
    void testSaveTransactionsV2_NegativeBatchValues() {
        when(cbtOwnerService.getPropertiesID()).thenReturn(validOwners);
        TransactionBatchDTO dto = new TransactionBatchDTO();
        dto.setProperty("V100");
        dto.setType("PROPIETARIO");
        dto.setRepresentaIngreso(true);

        BraceletBatchDTO batch = new BraceletBatchDTO();
        batch.setSerialInicial(-5);
        batch.setSerialFinal(10);
        batch.setCantidad(15);
        dto.setBatches(Collections.singletonList(batch));

        ResponseEntity<?> response = braceletService.saveTransactionsV2(Collections.singletonList(dto));
        assertEquals(400, response.getStatusCode().value());
        assertTrue(java.util.Objects.requireNonNull(response.getBody()).toString().contains("NEGATIVE"));
    }

    @Test
    void testSaveTransactionsV2_Success() {
        when(cbtOwnerService.getPropertiesID()).thenReturn(validOwners);

        TransactionBatchDTO dto = new TransactionBatchDTO();
        dto.setProperty("V100");
        dto.setType("PROPIETARIO");
        dto.setRepresentaIngreso(true);
        dto.setDeliveryDate("2026-01-01 12:00");

        BraceletBatchDTO batch = new BraceletBatchDTO();
        batch.setSerialInicial(101);
        batch.setSerialFinal(110);
        batch.setCantidad(10);
        batch.setPrice(5.0);
        dto.setBatches(Collections.singletonList(batch));

        PaymentBatchDTO payment = new PaymentBatchDTO();
        payment.setPaymentMethod("ZELLE");
        payment.setAmount(50.0);
        payment.setTasaBcv(1.0);
        payment.setSerialBilletePagoUsd("ZELLE12345");
        dto.setPayments(Collections.singletonList(payment));

        LoteEntity lote = new LoteEntity();
        lote.setFirstSerial(101);
        lote.setQuantity(100);
        lote.setRemaining(100);
        when(loteRepo.findLoteBySerialAndType(101, "PROPIETARIO")).thenReturn(Collections.singletonList(lote));

        ResponseEntity<?> response = braceletService.saveTransactionsV2(Collections.singletonList(dto));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("SUCCESS", response.getBody());
        verify(transactionV2Repository, times(1)).saveAll(any());
        verify(notificationService, times(1)).sendNotification(anyString());
        assertEquals(90, lote.getRemaining()); // Inventory deducted
    }

    @Test
    void testSaveTransactionsV2_SendsWhatsappForInvitado() {
        when(cbtOwnerService.getPropertiesID()).thenReturn(validOwners);

        TransactionBatchDTO dto = new TransactionBatchDTO();
        dto.setProperty("V100");
        dto.setType("INVITADO");
        dto.setRepresentaIngreso(true);
        dto.setDeliveryDate("2026-01-01 12:00");

        BraceletBatchDTO batch = new BraceletBatchDTO();
        batch.setSerialInicial(15001);
        batch.setSerialFinal(15005);
        batch.setCantidad(5);
        batch.setPrice(5.0);
        dto.setBatches(Collections.singletonList(batch));

        PaymentBatchDTO payment = new PaymentBatchDTO();
        payment.setPaymentMethod("ZELLE");
        payment.setAmount(25.0);
        payment.setTasaBcv(1.0);
        payment.setSerialBilletePagoUsd("ZELLE12345");
        dto.setPayments(Collections.singletonList(payment));

        LoteEntity lote = new LoteEntity();
        lote.setFirstSerial(15001);
        lote.setQuantity(100);
        lote.setRemaining(100);
        when(loteRepo.findLoteBySerialAndType(15001, "INVITADO")).thenReturn(Collections.singletonList(lote));

        PropietarioModel owner = new PropietarioModel();
        owner.setFullName("Owner Test");
        owner.setTelefono("584121234567");
        owner.setCupoRestanteBrazalete(12);
        when(cbtOwnerService.getOwnerById(1L)).thenReturn(owner);

        ResponseEntity<?> response = braceletService.saveTransactionsV2(Collections.singletonList(dto));

        assertEquals(200, response.getStatusCode().value());
        verify(whatsappService, times(1)).sendBraceletNotification("584121234567", "Owner Test", 5, "INVITADO", "V100", 12);
        verify(cbtOwnerService, never()).update(anyLong(), anyInt());
    }

    @Test
    void testDeleteTransaction_Success() {
        TransactionEntityV2 tx = new TransactionEntityV2();
        tx.setId(1L);
        tx.setType("PROPIETARIO");
        BraceletBatchEntity batch = new BraceletBatchEntity();
        batch.setSerialInicial(101);
        batch.setCantidad(10);
        tx.setBatches(Collections.singletonList(batch));

        when(transactionV2Repository.findById(1L)).thenReturn(Optional.of(tx));
        LoteEntity lote = new LoteEntity();
        lote.setRemaining(90);
        when(loteRepo.findLoteBySerialAndType(101, "PROPIETARIO")).thenReturn(Collections.singletonList(lote));

        braceletService.deleteTransaction(1L);

        assertEquals(100, lote.getRemaining()); // Inventory restored
        verify(transactionV2Repository, times(1)).delete(tx);
        verify(notificationService, times(1)).sendNotification(anyString());
    }

    @Test
    void testReportingMethods() {
        when(transactionV2Repository.sumCantidadByDeliveryDateBetween(anyString(), anyString())).thenReturn(50);
        assertEquals(50, braceletService.getGeneralDayCount("date1", "date2"));

        when(transactionV2Repository.findMaxSerialFinal()).thenReturn(999);
        assertEquals(999, braceletService.getMaxSerial());

        when(loteRepo.sumRemaining()).thenReturn(5000);
        assertEquals(5000, braceletService.getRemaining());
    }
}
