package com.helios.platform.pulse.service;

import com.helios.platform.pulse.dto.BraceletBatchDTO;
import com.helios.platform.pulse.dto.PaymentBatchDTO;
import com.helios.platform.pulse.dto.TransactionBatchDTO;
import com.helios.platform.pulse.entities.BraceletBatchEntity;
import com.helios.platform.pulse.entities.PaymentEntityV2;
import com.helios.platform.pulse.entities.TransactionEntityV2;

import com.helios.platform.pulse.repositories.ILoteRepo;
import com.helios.platform.pulse.repositories.ITransactionV2;
import com.helios.platform.pulse.entities.LoteEntity;
import org.springframework.cache.annotation.CacheEvict;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.helios.platform.pulse.mapper.TransactionMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BraceletServiceV2 {

    private final ITransactionV2 transactionV2Repository;
    private final PulseOwnerService cbtOwnerService;
    private final NotificationService notificationService;
    private final TransactionMapper transactionMapper;
    private final ILoteRepo loteRepo;
    private final WhatsAppService whatsappService;

    @Autowired
    public BraceletServiceV2(ITransactionV2 transactionV2Repository,
                             PulseOwnerService cbtOwnerService,
                             NotificationService notificationService,
                             TransactionMapper transactionMapper,
                             ILoteRepo loteRepo,
                             WhatsAppService whatsappService) {
        this.transactionV2Repository = transactionV2Repository;
        this.cbtOwnerService = cbtOwnerService;
        this.notificationService = notificationService;
        this.transactionMapper = transactionMapper;
        this.loteRepo = loteRepo;
        this.whatsappService = whatsappService;
    }

    @Transactional
    @CacheEvict(value = "dailyReports", allEntries = true)
    public ResponseEntity<?> saveTransactionsV2(List<TransactionBatchDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return ResponseEntity.badRequest().body("NO TRANSACTIONS PROVIDED");
        }

        HashMap<String, Long> ownersHashMap = cbtOwnerService.getPropertiesID();
        if (ownersHashMap == null) {
            return ResponseEntity.internalServerError().body("SERVICE UNAVAILABLE: Unable to load properties data.");
        }

        List<TransactionEntityV2> entitiesToSave = new ArrayList<>();
        int totalBraceletsCount = 0;

        for (TransactionBatchDTO t : dtoList) {
            String property = t.getProperty() != null ? t.getProperty().toUpperCase() : "";

            if (property.isBlank() || property.isEmpty()) {
                return ResponseEntity.badRequest().body("NOT ALLOWED EMPTY OR BLANK PROPERTY FIELD");
            }
            if (!ownersHashMap.containsKey(property)) {
                return ResponseEntity.badRequest().body("PROPERTY MUST BE VALID");
            }
            if (!isValidType(t.getType())) {
                return ResponseEntity.badRequest().body("NOT ALLOWED TYPE OF BRACELET");
            }

            // Validation: Compute total expected cost vs total paid amount
            if (Boolean.TRUE.equals(t.getRepresentaIngreso())) {
                double expectedTotalUsd = 0.0;
                if (t.getBatches() != null) {
                    for (BraceletBatchDTO b : t.getBatches()) {
                        if (b.getSerialInicial() == null || b.getSerialFinal() == null || b.getCantidad() == null) {
                            return ResponseEntity.badRequest().body("SERIAL NUMBER AND QUANTITY CANNOT BE NULL");
                        }
                        // Puntos 1 y 2: Validar Lógica Inversa y Valores Negativos
                        if (b.getSerialInicial() < 0 || b.getSerialFinal() < 0 || b.getCantidad() < 0 || (b.getPrice() != null && b.getPrice() < 0)) {
                            return ResponseEntity.badRequest().body("NEGATIVE VALUES ARE NOT ALLOWED IN BRACELETS");
                        }
                        if (b.getSerialInicial() > b.getSerialFinal()) {
                            return ResponseEntity.badRequest().body("INVALID SERIAL RANGE: Initial cannot be greater than Final");
                        }
                        if (((b.getSerialFinal() - b.getSerialInicial()) + 1) != b.getCantidad()) {
                            return ResponseEntity.badRequest().body("MATHEMATICAL INCONSISTENCY: Bracelet quantity does not match the serial range");
                        }

                        double unitPrice = b.getPrice() != null ? b.getPrice() : 5.0;
                        expectedTotalUsd += b.getCantidad() * unitPrice;
                    }
                }

                double totalPaidUsd = 0.0;
                if (t.getPayments() != null) {
                    for (PaymentBatchDTO p : t.getPayments()) {
                        // Punto 2: Validar Montos Negativos
                        if (p.getAmount() != null && p.getAmount() < 0) {
                            return ResponseEntity.badRequest().body("NEGATIVE VALUES ARE NOT ALLOWED IN PAYMENTS");
                        }
                        if (p.getTasaBcv() != null && p.getTasaBcv() < 0) {
                            return ResponseEntity.badRequest().body("NEGATIVE EXCHANGE RATE IS NOT ALLOWED");
                        }

                        // VALIDATION: All payment methods except EFECTIVO_BS must have a reference number
                        String method = p.getPaymentMethod() != null ? p.getPaymentMethod().toUpperCase() : "";
                        boolean isUsdMethod = "ZELLE".equals(method) || "EFECTIVO".equals(method) || "PAGO_USD".equals(method) || "USD_CASH".equals(method) || "CASH_USD".equals(method);
                        boolean isEfectivoBs = "EFECTIVO_BS".equals(method) || "EFECTIVO BS".equals(method);

                        if (!isEfectivoBs) {
                            if (isUsdMethod) {
                                if (p.getSerialBilletePagoUsd() == null || p.getSerialBilletePagoUsd().trim().isEmpty()) {
                                    return ResponseEntity.badRequest().body("REFERENCE OR BILL SERIAL IS REQUIRED FOR PAYMENT METHOD: " + method);
                                }
                            } else {
                                String ref = p.getReferenciaPagoBs();
                                if (ref == null || ref.trim().isEmpty()) {
                                    ref = p.getSerialBilletePagoUsd();
                                }
                                if (ref == null || ref.trim().isEmpty()) {
                                    return ResponseEntity.badRequest().body("REFERENCE NUMBER IS REQUIRED FOR PAYMENT METHOD: " + method);
                                }
                            }
                        }

                        if (p.getAmount() != null) {
                            // Si el exchange rate es mayor a 1 Y NO es un método en dólares, asumimos que el amount es en Bolívares y lo dividimos
                            if (!isUsdMethod && p.getTasaBcv() != null && p.getTasaBcv() > 1.0) {
                                totalPaidUsd += p.getAmount() / p.getTasaBcv();
                            } else {
                                // De lo contrario, el amount ya viene en USD (o la tasa es 1.0)
                                totalPaidUsd += p.getAmount();
                            }
                        }
                    }
                }

                // Tolerancia de 0.02 centavos para problemas de redondeo.
                // Permitimos que el pago sea MAYOR al esperado (por errores del cliente al transferir de más), pero NUNCA menor.
                if (totalPaidUsd < (expectedTotalUsd - 0.02)) {
                    return ResponseEntity.badRequest().body("AMOUNTS DO NOT MATCH. Expected AT LEAST: " + expectedTotalUsd + " USD, Computed Paid: " + totalPaidUsd + " USD");
                }
            }

            TransactionEntityV2 transaction = new TransactionEntityV2();
            transaction.setProperty(property);

            // Normalizar deliveryDate: el frontend envía "YYYY-MM-DD HH:MM".
            // Si llega incompleto (solo fecha sin hora), el backend estampa la hora actual.
            String rawDeliveryDate = t.getDeliveryDate();
            String normalizedDeliveryDate;
            if (rawDeliveryDate == null || rawDeliveryDate.isBlank()) {
                normalizedDeliveryDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } else if (rawDeliveryDate.trim().length() == 10) {
                // Solo fecha sin hora → añadir hora actual del servidor
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                normalizedDeliveryDate = rawDeliveryDate.trim() + " " + currentTime;
            } else {
                normalizedDeliveryDate = rawDeliveryDate.trim();
            }
            transaction.setDeliveryDate(normalizedDeliveryDate);

            // Permitir el nombre del operador seleccionado en la UI
            // Opcionalmente se podria registrar el cajero en otro campo o log
            transaction.setResponsableNombre(t.getResponsableNombre());

            transaction.setWorkshift(t.getWorkshift());
            transaction.setType(t.getType());
            transaction.setRepresentaIngreso(t.getRepresentaIngreso());
            transaction.setObservacion(t.getObservacion());

            // Map Payments
            List<PaymentEntityV2> paymentEntities = new ArrayList<>();
            if (t.getPayments() != null) {
                for (PaymentBatchDTO p : t.getPayments()) {
                    PaymentEntityV2 payment = new PaymentEntityV2();
                    payment.setTransaction(transaction);
                    payment.setPaymentMethod(p.getPaymentMethod());
                    payment.setAmount(p.getAmount());
                    payment.setExchangeRate(p.getTasaBcv());
                    payment.setReferenciaPagoBs(p.getReferenciaPagoBs());
                    payment.setSerialBilletePagoUsd(p.getSerialBilletePagoUsd());
                    paymentEntities.add(payment);
                }
            }
            transaction.setPayments(paymentEntities);

            // Map Bracelet Batches
            List<BraceletBatchEntity> batchEntities = new ArrayList<>();
            if (t.getBatches() != null) {
                for (BraceletBatchDTO b : t.getBatches()) {
                    BraceletBatchEntity entity = new BraceletBatchEntity();
                    entity.setTransaction(transaction);
                    entity.setSerialInicial(b.getSerialInicial());
                    entity.setSerialFinal(b.getSerialFinal());
                    entity.setCantidad(b.getCantidad());
                    entity.setPrice(b.getPrice() != null ? b.getPrice() : 5.0);

                    batchEntities.add(entity);
                    totalBraceletsCount += b.getCantidad();

                    // Deduct inventory
                    List<LoteEntity> lotes = loteRepo.findLoteBySerialAndType(b.getSerialInicial(), t.getType());
                    if (lotes.isEmpty()) {
                        return ResponseEntity.status(409).body("SYNC ERROR: El serial " + b.getSerialInicial() + " no pertenece a ningún lote activo.");
                    }
                    LoteEntity lote = lotes.get(0);
                    int expectedSerial = lote.getFirstSerial() + (lote.getQuantity() - lote.getRemaining());
                    if (b.getSerialInicial() != expectedSerial) {
                        return ResponseEntity.status(409).body("SYNC ERROR: Desajuste de seriales. El servidor esperaba: " + expectedSerial + " pero recibió: " + b.getSerialInicial());
                    }
                    if (b.getCantidad() > lote.getRemaining()) {
                        return ResponseEntity.status(409).body("SYNC ERROR: Lote agotado o insuficiente. Restante: " + lote.getRemaining());
                    }
                    lote.setRemaining(lote.getRemaining() - b.getCantidad());
                    loteRepo.save(lote);

                    Long ownerId = ownersHashMap.get(property);
                    if (ownerId != null) {
                        // Descontar cupo del apartamento SOLO si es de propietario
                        if ("PROPIETARIO".equalsIgnoreCase(t.getType())) {
                            cbtOwnerService.update(ownerId, b.getCantidad());
                        }

                        // Notificar siempre al teléfono del propietario del inmueble,
                        // incluso para brazaletes INVITADO y ALQUILADO.
                        com.helios.platform.pulse.entities.PropietarioModel owner = cbtOwnerService.getOwnerById(ownerId);
                        if (owner != null && owner.getTelefono() != null && !owner.getTelefono().isEmpty()) {
                            int remainingAfterTx = owner.getCupoRestanteBrazalete() != null ? owner.getCupoRestanteBrazalete() : 0;
                            whatsappService.sendBraceletNotification(
                                owner.getTelefono(),
                                owner.getFullName(),
                                b.getCantidad(),
                                t.getType(),
                                property,
                                remainingAfterTx
                            );
                        }
                    }
                }
            }
            transaction.setBatches(batchEntities);

            entitiesToSave.add(transaction);
        }

        transactionV2Repository.saveAll(entitiesToSave);
        notificationService.sendNotification("V2 SUCCESS OPERATION: " + entitiesToSave.size() + " transactions saved. Total Bracelets: " + totalBraceletsCount);
        return ResponseEntity.ok().body("SUCCESS");
    }

    @Transactional
    @CacheEvict(value = "dailyReports", allEntries = true)
    public void deleteTransaction(Long id) {
        TransactionEntityV2 transaction = transactionV2Repository.findById(id).orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Restore inventory
        HashMap<String, Long> ownersMap = null;
        if ("PROPIETARIO".equalsIgnoreCase(transaction.getType())) {
            ownersMap = cbtOwnerService.getPropertiesID();
        }

        if (transaction.getBatches() != null) {
            for (BraceletBatchEntity b : transaction.getBatches()) {
                List<LoteEntity> lotes = loteRepo.findLoteBySerialAndType(b.getSerialInicial(), transaction.getType());
                if (!lotes.isEmpty()) {
                    LoteEntity lote = lotes.get(0);
                    lote.setRemaining(lote.getRemaining() + b.getCantidad());
                    loteRepo.save(lote);
                }

                // Reembolsar cupo del apartamento si la transacción fue eliminada
                if ("PROPIETARIO".equalsIgnoreCase(transaction.getType()) && ownersMap != null) {
                    Long ownerId = ownersMap.get(transaction.getProperty());
                    if (ownerId != null) {
                        cbtOwnerService.update(ownerId, -b.getCantidad()); // Un valor negativo SUMA al inventario
                    }
                }
            }
        }
        transactionV2Repository.delete(transaction);
        notificationService.sendNotification("V2 SUCCESS DELETE: Transaction " + id + " deleted and inventory restored.");
    }

    private boolean isValidType(String type) {
        if (type == null) return false;
        String upperType = type.toUpperCase();
        return "PROPIETARIO".equals(upperType)
                || "INVITADO".equals(upperType)
                || "ALQUILADO".equals(upperType);
    }

    public Integer getGeneralDayCount(String fromDate, String toDate) {
        return transactionV2Repository.sumCantidadByDeliveryDateBetween(fromDate, toDate);
    }

    public List<TransactionBatchDTO> getGeneralDayReport(String fromDate, String toDate) {
        return transactionMapper.toDtoList(transactionV2Repository.findByDeliveryDateBetweenOrderByDeliveryDateAsc(fromDate, toDate));
    }

    public Integer getCustomTypeCount(String fromDate, String toDate, String type) {
        return transactionV2Repository.sumCantidadByDeliveryDateBetweenAndType(fromDate, toDate, type);
    }

    public List<TransactionBatchDTO> getCustomTypeReport(String fromDate, String toDate, String type) {
        return transactionMapper.toDtoList(transactionV2Repository.findByDeliveryDateBetweenAndTypeOrderByDeliveryDateAsc(fromDate, toDate, type));
    }

    public Integer getCustomWorkshiftCount(String fromDate, String toDate, String workshift) {
        return transactionV2Repository.sumCantidadByDeliveryDateBetweenAndWorkshift(fromDate, toDate, workshift);
    }

    public List<TransactionBatchDTO> getCustomWorkshiftReport(String fromDate, String toDate, String workshift) {
        return transactionMapper.toDtoList(transactionV2Repository.findByDeliveryDateBetweenAndWorkshiftOrderByDeliveryDateAsc(fromDate, toDate, workshift));
    }

    public Integer getCustomCount(String fromDate, String toDate, String type, String workshift) {
        return transactionV2Repository.sumCantidadByDeliveryDateBetweenAndTypeAndWorkshift(fromDate, toDate, type, workshift);
    }

    public List<TransactionBatchDTO> getCustomReport(String fromDate, String toDate, String type, String workshift) {
        return transactionMapper.toDtoList(transactionV2Repository.findByDeliveryDateBetweenAndTypeAndWorkshiftOrderByDeliveryDateAsc(fromDate, toDate, type, workshift));
    }

    public Integer getCustomMultiTypeCount(String fromDate, String toDate, List<String> types) {
        return transactionV2Repository.sumCantidadByDeliveryDateBetweenAndTypeIn(fromDate, toDate, types);
    }

    public List<TransactionBatchDTO> getCustomMultiTypeReport(String fromDate, String toDate, List<String> types) {
        return transactionMapper.toDtoList(transactionV2Repository.findByDeliveryDateBetweenAndTypeInOrderByDeliveryDateAsc(fromDate, toDate, types));
    }

    public Integer getCustomMultiTypeAndWorkshiftCount(String fromDate, String toDate, List<String> types, String workshift) {
        return transactionV2Repository.sumCantidadByDeliveryDateBetweenAndTypeInAndWorkshift(fromDate, toDate, types, workshift);
    }

    public List<TransactionBatchDTO> getCustomMultiTypeAndWorkshiftReport(String fromDate, String toDate, List<String> types, String workshift) {
        return transactionMapper.toDtoList(transactionV2Repository.findByDeliveryDateBetweenAndTypeInAndWorkshiftOrderByDeliveryDateAsc(fromDate, toDate, types, workshift));
    }

    public com.helios.platform.pulse.dto.CashCloseDTO getCashClose(String date, String type) {
        String queryDate = date;
        if (queryDate != null && queryDate.length() > 10) {
            queryDate = queryDate.substring(0, 10);
        }

        String fromDateFull = (queryDate != null && !queryDate.isBlank()) ? queryDate.trim() + " 00:00" : "";
        String toDateFull = (queryDate != null && !queryDate.isBlank()) ? queryDate.trim() + " 23:59" : "";

        List<TransactionEntityV2> transactions = transactionV2Repository.findByDeliveryDateBetweenAndTypeOrderByDeliveryDateAsc(fromDateFull, toDateFull, type);

        int totalBracelets = 0;
        double totalAmountUsd = 0.0;
        HashMap<String, com.helios.platform.pulse.dto.PaymentMethodSummaryDTO> summaryMap = new HashMap<>();

        for (TransactionEntityV2 tx : transactions) {
            if (tx.getBatches() != null) {
                for (BraceletBatchEntity b : tx.getBatches()) {
                    totalBracelets += b.getCantidad() != null ? b.getCantidad() : 0;
                }
            }
            if (tx.getPayments() != null) {
                for (PaymentEntityV2 p : tx.getPayments()) {
                    String method = p.getPaymentMethod() != null ? p.getPaymentMethod().toUpperCase() : "UNKNOWN";
                    double amount = p.getAmount() != null ? p.getAmount() : 0.0;
                    double rate = p.getExchangeRate() != null ? p.getExchangeRate() : 1.0;

                    boolean isUsdMethod = "ZELLE".equals(method) || "EFECTIVO".equals(method);
                    double amountUsd = 0.0;

                    if (!isUsdMethod && rate > 1.0) {
                        amountUsd = amount / rate;
                    } else {
                        amountUsd = amount;
                    }

                    totalAmountUsd += amountUsd;

                    com.helios.platform.pulse.dto.PaymentMethodSummaryDTO summary = summaryMap.computeIfAbsent(method, k -> {
                        com.helios.platform.pulse.dto.PaymentMethodSummaryDTO s = new com.helios.platform.pulse.dto.PaymentMethodSummaryDTO();
                        s.setPaymentMethod(k);
                        s.setTotalAmount(0.0);
                        s.setTotalAmountUsd(0.0);
                        return s;
                    });
                    summary.setTotalAmount(summary.getTotalAmount() + amount);
                    summary.setTotalAmountUsd(summary.getTotalAmountUsd() + amountUsd);
                }
            }
        }

        com.helios.platform.pulse.dto.CashCloseDTO cashClose = new com.helios.platform.pulse.dto.CashCloseDTO();
        cashClose.setDate(queryDate);
        cashClose.setType(type);
        cashClose.setTotalBracelets(totalBracelets);
        cashClose.setTotalAmountUsd(totalAmountUsd);
        cashClose.setPaymentsSummary(new ArrayList<>(summaryMap.values()));

        return cashClose;
    }

    public Integer getMaxSerial() {
        return transactionV2Repository.findMaxSerialFinal();
    }

    public Integer getRemaining() {
        return loteRepo.sumRemaining();
    }

    public List<com.helios.platform.pulse.dto.OwnerHistoryDTO> getOwnerHistory(String property) {
        List<TransactionEntityV2> transactions = transactionV2Repository.findByPropertyOrderByDeliveryDateDesc(property);
        List<com.helios.platform.pulse.dto.OwnerHistoryDTO> history = new ArrayList<>();

        for (TransactionEntityV2 tx : transactions) {
            com.helios.platform.pulse.dto.OwnerHistoryDTO dto = new com.helios.platform.pulse.dto.OwnerHistoryDTO();
            dto.setTransactionId(tx.getId());
            dto.setNum_property(tx.getProperty());
            dto.setType(tx.getType());
            dto.setDeliveryDate(tx.getDeliveryDate());
            dto.setResponsable(tx.getResponsableNombre());

            int totalQuantity = 0;
            StringBuilder serialsBuilder = new StringBuilder();
            if (tx.getBatches() != null) {
                for (BraceletBatchEntity batch : tx.getBatches()) {
                    totalQuantity += batch.getCantidad() != null ? batch.getCantidad() : 0;
                    if (batch.getSerialInicial() != null && batch.getSerialFinal() != null) {
                        if (serialsBuilder.length() > 0) serialsBuilder.append(", ");
                        serialsBuilder.append(batch.getSerialInicial()).append("-").append(batch.getSerialFinal());
                    }
                }
            }
            dto.setQuantity(totalQuantity);
            dto.setSerials(serialsBuilder.toString());
            dto.setObservacion(tx.getObservacion());

            double totalAmount = 0.0;
            StringBuilder methodsBuilder = new StringBuilder();
            StringBuilder refsBuilder = new StringBuilder();
            if (tx.getPayments() != null) {
                for (PaymentEntityV2 payment : tx.getPayments()) {
                    if (payment.getAmount() != null) {
                        totalAmount += payment.getAmount();
                    }
                    if (payment.getPaymentMethod() != null) {
                        if (methodsBuilder.indexOf(payment.getPaymentMethod()) == -1) {
                            if (methodsBuilder.length() > 0) methodsBuilder.append(", ");
                            methodsBuilder.append(payment.getPaymentMethod());
                        }
                    }
                    if (payment.getReferenciaPagoBs() != null && !payment.getReferenciaPagoBs().isBlank()) {
                        if (refsBuilder.length() > 0) refsBuilder.append(", ");
                        refsBuilder.append(payment.getReferenciaPagoBs());
                    }
                    if (payment.getSerialBilletePagoUsd() != null && !payment.getSerialBilletePagoUsd().isBlank()) {
                        if (refsBuilder.length() > 0) refsBuilder.append(", ");
                        refsBuilder.append(payment.getSerialBilletePagoUsd());
                    }
                }
            }
            dto.setTotalAmount(totalAmount);
            dto.setPaymentMethods(methodsBuilder.toString());
            dto.setPaymentReferences(refsBuilder.toString());

            history.add(dto);
        }
        return history;
    }
}
