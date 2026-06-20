package com.fabriciosanches.adapter.inbound.rest;

import com.fabriciosanches.domain.port.input.GetPaymentStatusCommand;
import com.fabriciosanches.domain.port.input.GetPaymentStatusResult;
import com.fabriciosanches.domain.port.input.GetPaymentStatusUseCasePort;
import com.fabriciosanches.domain.port.input.ProcessPaymentCallbackCommand;
import com.fabriciosanches.domain.port.input.ProcessPaymentCallbackResult;
import com.fabriciosanches.domain.port.input.ProcessPaymentCallbackUseCasePort;
import com.fabriciosanches.domain.port.input.ProcessPaymentCommand;
import com.fabriciosanches.domain.port.input.ProcessPaymentResult;
import com.fabriciosanches.domain.port.input.ProcessPaymentUseCasePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Gerenciamento de pagamentos")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final ProcessPaymentUseCasePort processPaymentUseCase;
    private final GetPaymentStatusUseCasePort getPaymentStatusUseCase;
    private final ProcessPaymentCallbackUseCasePort processPaymentCallbackUseCase;

    public PaymentController(ProcessPaymentUseCasePort processPaymentUseCase,
                             GetPaymentStatusUseCasePort getPaymentStatusUseCase,
                             ProcessPaymentCallbackUseCasePort processPaymentCallbackUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
        this.getPaymentStatusUseCase = getPaymentStatusUseCase;
        this.processPaymentCallbackUseCase = processPaymentCallbackUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Iniciar pagamento", description = "Inicia o processamento de pagamento para um pedido")
    ProcessPaymentResponse process(@Valid @RequestBody ProcessPaymentRequest request) {
        ProcessPaymentResult result = processPaymentUseCase.execute(
                new ProcessPaymentCommand(request.orderId(), request.paymentMethod())
        );
        return new ProcessPaymentResponse(result.paymentId(), result.status());
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Consultar status do pagamento", description = "Retorna o status atual de um pagamento")
    GetPaymentStatusResult getPaymentStatus(@PathVariable("paymentId") String paymentId) {
        return getPaymentStatusUseCase.execute(new GetPaymentStatusCommand(paymentId));
    }

    @PostMapping("/{paymentId}/callback")
    @Operation(summary = "Processar callback do gateway", description = "Processa o webhook de resposta do gateway de pagamento (idempotente)")
    ProcessPaymentCallbackResult processCallback(@PathVariable("paymentId") String paymentId,
                                                 @Valid @RequestBody PaymentCallbackRequest request) {
        return processPaymentCallbackUseCase.execute(
                new ProcessPaymentCallbackCommand(paymentId, request.status()));
    }

    public record ProcessPaymentRequest(@NotBlank String orderId,
                                        @NotBlank String paymentMethod) {
    }

    public record ProcessPaymentResponse(String paymentId, String status) {
    }

    public record PaymentCallbackRequest(@NotBlank String status) {
    }
}
