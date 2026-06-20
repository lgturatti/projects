package com.fabriciosanches.adapter.outbound.http;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.port.output.CustomerClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CustomerClientAdapter implements CustomerClientPort {

    private static final Logger log = LoggerFactory.getLogger(CustomerClientAdapter.class);

    private final RestClient restClient;

    public CustomerClientAdapter(RestClient.Builder builder,
                                 @Value("${clients.customer.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "customerService", fallbackMethod = "validateCustomerFallback")
    public void validateCustomer(String customerId) {
        restClient.get()
                .uri("/customers/{id}", customerId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new DomainException(
                            "Cliente inválido ou inativo. customerId=" + customerId);
                })
                .toBodilessEntity();
    }

    private void validateCustomerFallback(String customerId, Exception ex) {
        if (ex instanceof DomainException domainException) {
            throw domainException;
        }
        log.error("Circuit breaker aberto para customerService. customerId={}", customerId, ex);
        throw new DomainException("Serviço de clientes temporariamente indisponível.");
    }
}
