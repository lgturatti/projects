package com.fabriciosanches.adapter.outbound.http;

import com.fabriciosanches.domain.exception.DomainException;
import com.fabriciosanches.domain.port.output.PaymentGatewayClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class PaymentGatewayClientAdapter implements PaymentGatewayClientPort {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayClientAdapter.class);

    private final RestClient restClient;

    public PaymentGatewayClientAdapter(RestClient.Builder builder,
                                       @Value("${clients.payment-gateway.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "chargeFallback")
    public PaymentGatewayResult charge(String orderId, BigDecimal amount, String paymentMethod) {
        ChargeRequest body = new ChargeRequest(orderId, amount, paymentMethod);
        ChargeResponse response = restClient.post()
                .uri("/payment-gateway/charges")
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, (request, resp) -> {
                    throw new DomainException(
                            "Gateway de pagamento indisponível. orderId=" + orderId);
                })
                .onStatus(HttpStatusCode::is4xxClientError, (request, resp) -> {
                    throw new DomainException(
                            "Requisição inválida ao gateway de pagamento. orderId=" + orderId);
                })
                .body(ChargeResponse.class);
        if (response == null) {
            throw new DomainException("Resposta inválida do gateway de pagamento. orderId=" + orderId);
        }
        return new PaymentGatewayResult(response.transactionId(), response.status());
    }

    private PaymentGatewayResult chargeFallback(String orderId, BigDecimal amount, String paymentMethod, Exception ex) {
        if (ex instanceof DomainException domainException) {
            throw domainException;
        }
        log.error("Circuit breaker aberto para paymentGateway. orderId={}", orderId, ex);
        throw new DomainException("Gateway de pagamento temporariamente indisponível.");
    }

    record ChargeRequest(String orderId, BigDecimal amount, String paymentMethod) {
    }

    record ChargeResponse(String transactionId, String status) {
    }
}
