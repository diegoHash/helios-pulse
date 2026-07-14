package com.helios.platform.pulse.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;

@Service
public class PaymentMethodService {

    public static final String EFECTIVO_USD = "PAGO_USD";
    public static final String PAGO_MOVIL = "PAGO_MOVIL";
    public static final String PUNTO_DE_VENTA = "PUNTO_DE_VENTA";
    public static final String EFECTIVO_BS = "EFECTIVO_BS";
    public static final String ZELLE = "ZELLE";

    private final HashMap<String, String> paymentMethods = new HashMap<>();

    public PaymentMethodService() {
        paymentMethods.put(EFECTIVO_USD, "Usd");
        paymentMethods.put(ZELLE, "Usd");
        paymentMethods.put(PAGO_MOVIL, "Bs");
        paymentMethods.put(PUNTO_DE_VENTA, "Bs");
        paymentMethods.put(EFECTIVO_BS, "Bs");
    }

    public boolean isValidPaymentMethod(String paymentMethod) {
        return paymentMethods.containsKey(paymentMethod);
    }

    public boolean checkFields(String paymentMethod, String refbs, String refUsd) {
        String currency = paymentMethods.get(paymentMethod);
        if (currency == null || currency.isEmpty()) {
            return false;
        }

        // According to original logic, Bs requires refbs and Usd requires refUsd
        if (currency.equals("Bs")) {
            return refbs != null && !refbs.isEmpty() && !refbs.isBlank();
        } else if (currency.equals("Usd")) {
            return refUsd != null && !refUsd.isEmpty() && !refUsd.isBlank();
        }
        return false;
    }
}
