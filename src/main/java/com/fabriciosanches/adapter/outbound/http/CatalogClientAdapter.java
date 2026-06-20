package com.fabriciosanches.adapter.outbound.http;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.port.output.CatalogClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class CatalogClientAdapter implements CatalogClientPort {

    private static final Logger log = LoggerFactory.getLogger(CatalogClientAdapter.class);

    private final RestClient restClient;

    public CatalogClientAdapter(RestClient.Builder builder,
                                @Value("${clients.catalog.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "catalogService", fallbackMethod = "getProductPriceFallback")
    public BigDecimal getProductPrice(String productId) {
        ProductPriceResponse response = restClient.get()
                .uri("/catalog/products/{id}/price", productId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, resp) -> {
                    throw new DomainException("Produto não encontrado no catálogo. productId=" + productId);
                })
                .body(ProductPriceResponse.class);
        if (response == null || response.price() == null) {
            throw new DomainException("Resposta inválida do serviço de catálogo para productId=" + productId);
        }
        return response.price();
    }

    @Override
    @CircuitBreaker(name = "catalogService", fallbackMethod = "validateStockFallback")
    public void validateStock(String productId, int quantity) {
        restClient.get()
                .uri("/catalog/products/{id}/stock?quantity={qty}", productId, quantity)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new DomainException(
                            "Estoque insuficiente para o produto. productId=" + productId);
                })
                .toBodilessEntity();
    }

    private BigDecimal getProductPriceFallback(String productId, Exception ex) {
        if (ex instanceof DomainException domainException) {
            throw domainException;
        }
        log.error("Circuit breaker aberto para catalogService (getProductPrice). productId={}", productId, ex);
        throw new DomainException("Serviço de catálogo temporariamente indisponível.");
    }

    private void validateStockFallback(String productId, int quantity, Exception ex) {
        if (ex instanceof DomainException domainException) {
            throw domainException;
        }
        log.error("Circuit breaker aberto para catalogService (validateStock). productId={}", productId, ex);
        throw new DomainException("Serviço de catálogo temporariamente indisponível.");
    }

    record ProductPriceResponse(BigDecimal price) {
    }
}
