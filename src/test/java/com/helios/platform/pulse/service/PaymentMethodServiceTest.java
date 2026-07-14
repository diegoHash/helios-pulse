package com.helios.platform.pulse.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodServiceTest {

    private final PaymentMethodService paymentMethodService = new PaymentMethodService();

    @Test
    void testIsValidPaymentMethod() {
        assertTrue(paymentMethodService.isValidPaymentMethod(PaymentMethodService.EFECTIVO_USD));
        assertTrue(paymentMethodService.isValidPaymentMethod(PaymentMethodService.PAGO_MOVIL));
        assertTrue(paymentMethodService.isValidPaymentMethod(PaymentMethodService.PUNTO_DE_VENTA));
        assertTrue(paymentMethodService.isValidPaymentMethod(PaymentMethodService.EFECTIVO_BS));
        assertTrue(paymentMethodService.isValidPaymentMethod(PaymentMethodService.ZELLE));

        assertFalse(paymentMethodService.isValidPaymentMethod("PAYPAL"));
        assertFalse(paymentMethodService.isValidPaymentMethod(""));
        assertFalse(paymentMethodService.isValidPaymentMethod(null));
    }

    @Test
    void testCheckFields_Bs() {
        // Valid Bs
        assertTrue(paymentMethodService.checkFields(PaymentMethodService.PAGO_MOVIL, "ref123", ""));
        assertTrue(paymentMethodService.checkFields(PaymentMethodService.PUNTO_DE_VENTA, "ref123", null));

        // Invalid Bs
        assertFalse(paymentMethodService.checkFields(PaymentMethodService.PAGO_MOVIL, null, "refUsd"));
        assertFalse(paymentMethodService.checkFields(PaymentMethodService.PAGO_MOVIL, "", "refUsd"));
        assertFalse(paymentMethodService.checkFields(PaymentMethodService.PAGO_MOVIL, "   ", "refUsd"));
    }

    @Test
    void testCheckFields_Usd() {
        // Valid Usd
        assertTrue(paymentMethodService.checkFields(PaymentMethodService.EFECTIVO_USD, "", "serial123"));
        assertTrue(paymentMethodService.checkFields(PaymentMethodService.ZELLE, null, "serial123"));

        // Invalid Usd
        assertFalse(paymentMethodService.checkFields(PaymentMethodService.ZELLE, "refBs", null));
        assertFalse(paymentMethodService.checkFields(PaymentMethodService.ZELLE, "refBs", ""));
        assertFalse(paymentMethodService.checkFields(PaymentMethodService.ZELLE, "refBs", "   "));
    }

    @Test
    void testCheckFields_InvalidMethod() {
        assertFalse(paymentMethodService.checkFields("PAYPAL", "ref123", "serial123"));
    }
}
