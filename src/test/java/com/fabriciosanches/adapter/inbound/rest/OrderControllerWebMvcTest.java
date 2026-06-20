package com.fabriciosanches.adapter.inbound.rest;

import com.fabriciosanches.config.SecurityConfig;
import com.fabriciosanches.domain.exception.ResourceNotFoundException;
import com.fabriciosanches.domain.port.input.AddItemToOrderUseCasePort;
import com.fabriciosanches.domain.port.input.CancelOrderUseCasePort;
import com.fabriciosanches.domain.port.input.ConfirmOrderUseCasePort;
import com.fabriciosanches.domain.port.input.CreateOrderResult;
import com.fabriciosanches.domain.port.input.CreateOrderUseCasePort;
import com.fabriciosanches.domain.port.input.GetOrderByIdResult;
import com.fabriciosanches.domain.port.input.GetOrderByIdUseCasePort;
import com.fabriciosanches.domain.port.input.ListOrdersByCustomerResult;
import com.fabriciosanches.domain.port.input.ListOrdersByCustomerUseCasePort;
import com.fabriciosanches.domain.port.input.RemoveItemFromOrderUseCasePort;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "security.jwt.secret=01234567890123456789012345678901")
@DisplayName("OrderController - WebMvc")
class OrderControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateOrderUseCasePort createOrderUseCase;

    @MockBean
    private GetOrderByIdUseCasePort getOrderByIdUseCase;

    @MockBean
    private ListOrdersByCustomerUseCasePort listOrdersByCustomerUseCase;

    @MockBean
    private AddItemToOrderUseCasePort addItemToOrderUseCase;

    @MockBean
    private RemoveItemFromOrderUseCasePort removeItemFromOrderUseCase;

    @MockBean
    private ConfirmOrderUseCasePort confirmOrderUseCase;

    @MockBean
    private CancelOrderUseCasePort cancelOrderUseCase;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/orders
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve retornar 401 quando JWT estiver ausente")
    void shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"customer-1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    @DisplayName("Deve retornar 403 quando escopo orders:write não for enviado")
    void shouldReturn403WhenScopeIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders:read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"customer-1\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    @DisplayName("Deve criar pedido quando escopo orders:write for válido")
    void shouldCreateOrderWhenScopeIsValid() throws Exception {
        when(createOrderUseCase.execute(any())).thenReturn(new CreateOrderResult("order-123"));

        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderController.CreateOrderRequest("customer-1"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("order-123"));

        verify(createOrderUseCase).execute(any());
    }

    @Test
    @DisplayName("Deve retornar 400 para payload inválido")
    void shouldReturn400WhenPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/orders/{orderId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve retornar pedido quando encontrado")
    void shouldReturnOrderWhenFound() throws Exception {
        GetOrderByIdResult result = new GetOrderByIdResult(
                "order-123", "customer-1", "PENDENTE",
                BigDecimal.ZERO, 0, Instant.now(), List.of());
        when(getOrderByIdUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(get("/api/v1/orders/order-123")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders:read"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-123"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando pedido não for encontrado")
    void shouldReturn404WhenOrderNotFound() throws Exception {
        when(getOrderByIdUseCase.execute(any()))
                .thenThrow(new ResourceNotFoundException("Pedido não encontrado."));

        mockMvc.perform(get("/api/v1/orders/unknown-id")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders:read"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/orders?customerId=...
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve listar pedidos do cliente")
    void shouldListOrdersByCustomer() throws Exception {
        when(listOrdersByCustomerUseCase.execute(any())).thenReturn(new ListOrdersByCustomerResult(List.of()));

        mockMvc.perform(get("/api/v1/orders").param("customerId", "customer-1")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders:read"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").isArray());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/orders/{orderId}/items
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve adicionar item ao pedido")
    void shouldAddItemToOrder() throws Exception {
        GetOrderByIdResult result = new GetOrderByIdResult(
                "order-123", "customer-1", "PENDENTE",
                BigDecimal.ZERO, 0, Instant.now(), List.of());
        when(addItemToOrderUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/orders/order-123/items")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"p1\",\"productName\":\"Produto\",\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-123"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/orders/{orderId}/items/{itemId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve remover item do pedido")
    void shouldRemoveItemFromOrder() throws Exception {
        doNothing().when(removeItemFromOrderUseCase).execute(any());

        mockMvc.perform(delete("/api/v1/orders/order-123/items/p1")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders:write"))))
                .andExpect(status().isNoContent());

        verify(removeItemFromOrderUseCase).execute(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/orders/{orderId}/confirm
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve confirmar pedido")
    void shouldConfirmOrder() throws Exception {
        GetOrderByIdResult result = new GetOrderByIdResult(
                "order-123", "customer-1", "CONFIRMADO",
                new BigDecimal("50.00"), 0, Instant.now(), List.of());
        when(confirmOrderUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/orders/order-123/confirm")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders:write"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/orders/{orderId}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve cancelar pedido")
    void shouldCancelOrder() throws Exception {
        doNothing().when(cancelOrderUseCase).execute(any());

        mockMvc.perform(delete("/api/v1/orders/order-123")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders:write"))))
                .andExpect(status().isNoContent());

        verify(cancelOrderUseCase).execute(any());
    }

    @Test
    @DisplayName("Deve retornar 404 ao cancelar pedido inexistente")
    void shouldReturn404WhenCancellingNonExistentOrder() throws Exception {
        doThrow(new ResourceNotFoundException("Pedido não encontrado."))
                .when(cancelOrderUseCase).execute(any());

        mockMvc.perform(delete("/api/v1/orders/unknown-id")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("SCOPE_orders:write"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }
}
