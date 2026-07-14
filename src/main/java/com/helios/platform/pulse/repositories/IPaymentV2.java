package com.helios.platform.pulse.repositories;

import com.helios.platform.pulse.entities.PaymentEntityV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPaymentV2 extends JpaRepository<PaymentEntityV2, Long> {
}
