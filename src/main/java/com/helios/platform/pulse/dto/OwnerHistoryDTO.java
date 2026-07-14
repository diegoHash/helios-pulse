package com.helios.platform.pulse.dto;

public class OwnerHistoryDTO {
    private Long transactionId;
    private String num_property;
    private String type;
    private String deliveryDate;
    private String responsable;
    private Integer quantity;
    private String observacion;
    private String serials;
    private Double totalAmount;
    private String paymentMethods;
    private String paymentReferences;

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public String getNum_property() { return num_property; }
    public void setNum_property(String num_property) { this.num_property = num_property; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String getSerials() { return serials; }
    public void setSerials(String serials) { this.serials = serials; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethods() { return paymentMethods; }
    public void setPaymentMethods(String paymentMethods) { this.paymentMethods = paymentMethods; }

    public String getPaymentReferences() { return paymentReferences; }
    public void setPaymentReferences(String paymentReferences) { this.paymentReferences = paymentReferences; }
}
