package com.fabriciosanches.domain.port.output;

import com.fabriciosanches.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepositoryPort {

    Payment save(Payment payment);

    Optional<Payment> findById(String id);
}
