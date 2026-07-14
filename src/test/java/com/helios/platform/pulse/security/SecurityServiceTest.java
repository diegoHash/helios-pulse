package com.helios.platform.pulse.security;

import com.helios.platform.pulse.entities.TransactionEntityV2;
import com.helios.platform.pulse.repositories.ITransactionV2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private ITransactionV2 transactionRepo;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SecurityService securityService;

    @Test
    void testIsTransactionOwner_NullAuth() {
        assertFalse(securityService.isTransactionOwner(null, 1L));
    }

    @Test
    void testIsTransactionOwner_NullName() {
        when(authentication.getName()).thenReturn(null);
        assertFalse(securityService.isTransactionOwner(authentication, 1L));
    }

    @Test
    void testIsTransactionOwner_IsRoot() {
        when(authentication.getName()).thenReturn("root_user");

        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ROOT"))).when(authentication).getAuthorities();

        assertTrue(securityService.isTransactionOwner(authentication, 1L));
        verify(transactionRepo, never()).findById(anyLong());
    }

    @Test
    void testIsTransactionOwner_NotRoot_NotFoundTx() {
        when(authentication.getName()).thenReturn("user_name");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CAJERO"))).when(authentication).getAuthorities();

        when(transactionRepo.findById(1L)).thenReturn(Optional.empty());

        assertFalse(securityService.isTransactionOwner(authentication, 1L));
    }

    @Test
    void testIsTransactionOwner_NotRoot_NotOwner() {
        when(authentication.getName()).thenReturn("user_name");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CAJERO"))).when(authentication).getAuthorities();

        TransactionEntityV2 tx = new TransactionEntityV2();
        tx.setResponsableNombre("other_user");
        when(transactionRepo.findById(1L)).thenReturn(Optional.of(tx));

        assertFalse(securityService.isTransactionOwner(authentication, 1L));
    }

    @Test
    void testIsTransactionOwner_NotRoot_IsOwner() {
        when(authentication.getName()).thenReturn("user_name");
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CAJERO"))).when(authentication).getAuthorities();

        TransactionEntityV2 tx = new TransactionEntityV2();
        tx.setResponsableNombre("user_name");
        when(transactionRepo.findById(1L)).thenReturn(Optional.of(tx));

        assertTrue(securityService.isTransactionOwner(authentication, 1L));
    }
}
