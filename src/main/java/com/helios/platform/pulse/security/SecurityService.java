package com.helios.platform.pulse.security;

import com.helios.platform.pulse.entities.TransactionEntityV2;
import com.helios.platform.pulse.repositories.ITransactionV2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    private final ITransactionV2 transactionRepo;

    public SecurityService(ITransactionV2 transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    /**
     * Verifica si el usuario autenticado es el creador/responsable de la transacción,
     * o si es un usuario ROOT (admin) que tiene permiso absoluto.
     */
    public boolean isTransactionOwner(Authentication authentication, Long transactionId) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        // Si es ROOT, se le permite todo
        boolean isRoot = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ROOT"));
        if (isRoot) {
            return true;
        }

        // Buscar transacción y verificar si el responsable coincide con el usuario logueado
        TransactionEntityV2 tx = transactionRepo.findById(transactionId).orElse(null);
        if (tx == null) {
            return false; // Si no existe, bloqueamos (el controlador retornará 403 o 404 posteriormente)
        }

        return authentication.getName().equals(tx.getResponsableNombre());
    }
}
