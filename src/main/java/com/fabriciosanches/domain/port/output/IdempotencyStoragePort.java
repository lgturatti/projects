package com.fabriciosanches.domain.port.output;

public interface IdempotencyStoragePort {

    boolean exists(String idempotencyKey);

    void store(String idempotencyKey);
}
