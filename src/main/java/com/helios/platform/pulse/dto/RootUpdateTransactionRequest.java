package com.helios.platform.pulse.dto;

import lombok.Data;
import java.util.List;

@Data
public class RootUpdateTransactionRequest {
    private String fecha;
    private String tipoBrazalete;
    private String inmueble;
    private String observacion;
    private List<PaymentUpdateDto> pagos;

    @Data
    public static class PaymentUpdateDto {
        private Long id; // ID del pago a modificar (null si se quiere agregar uno nuevo en el futuro, pero por ahora solo editaremos)
        private Double monto;
        private String formaPago;
        private String referencia;
    }
}
