package com.fabriciosanches.adapter.inbound.rest;

import com.fabriciosanches.config.SecurityConfig;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.port.input.GetPaymentStatusResult;
import com.fabriciosanches.domain.port.input.GetPaymentStatusUseCasePort;
import com.fabriciosanches.domain.port.input.ProcessPaymentCallbackResult;
import com.fabriciosanches.domain.port.input.ProcessPaymentCallbackUseCasePort;
import com.fabriciosanches.domain.port.input.ProcessPaymentResult;
import com.fabriciosanches.domain.port.input.ProcessPaymentUseCasePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "security.jwt.secret=01234567890123456789012345678901")
@DisplayName("PaymentController - WebMvc")
class PaymentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProcessPaymentUseCasePort processPaymentUseCase;

    @MockBean
    private GetPaymentStatusUseCasePort getPaymentStatusUseCase;

    @MockBean
    private ProcessPaymentCallbackUseCasePort processPaymentCallbackUseCase;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/payments
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve retornar 401 quando JWT estiver ausente")
    void shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"order-1\",\"paymentMethod\":\"pix\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    @DisplayName("Deve retornar 403 quando escopo payments:write não for enviado")
    void shouldReturn403WhenScopeIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"order-1\",\"paymentMethod\":\"pix\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    @DisplayName("Deve processar pagamento quando escopo payments:write for válido")
    void shouldProcessPaymentWhenScopeIsValid() throws Exception {
        when(processPaymentUseCase.execute(any())).thenReturn(new ProcessPaymentResult("payment-123", "PROCESSING"));

        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_payments:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentController.ProcessPaymentRequest("order-1", "pix"))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.paymentId").value("payment-123"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        verify(processPaymentUseCase).execute(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/payments/{paymentId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve retornar status do pagamento quando encontrado")
    void shouldReturnPaymentStatusWhenFound() throws Exception {
        GetPaymentStatusResult result = new GetPaymentStatusResult(
                "payment-123", "order-1", "pix", "PROCESSING", Instant.now());
        when(getPaymentStatusUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(get("/api/v1/payments/payment-123")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_payments:read"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("payment-123"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando pagamento não for encontrado")
    void shouldReturn404WhenPaymentNotFound() throws Exception {
        when(getPaymentStatusUseCase.execute(any()))
                .thenThrow(new ResourceNotFoundException("Pagamento não encontrado."));

        mockMvc.perform(get("/api/v1/payments/unknown-id")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_payments:read"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/payments/{paymentId}/callback
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve processar callback de pagamento aprovado")
    void shouldProcessApprovedPaymentCallback() throws Exception {
        when(processPaymentCallbackUseCase.execute(any()))
                .thenReturn(new ProcessPaymentCallbackResult("payment-123", "APPROVED"));

        mockMvc.perform(post("/api/v1/payments/payment-123/callback")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_payments:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("payment-123"))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(processPaymentCallbackUseCase).execute(any());
    }

    @Test
    @DisplayName("Deve retornar 400 para callback com status inválido")
    void shouldReturn400ForCallbackWithInvalidStatus() throws Exception {
        mockMvc.perform(post("/api/v1/payments/payment-123/callback")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_payments:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
