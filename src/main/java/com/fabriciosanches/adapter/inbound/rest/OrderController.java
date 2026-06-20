package com.fabriciosanches.adapter.inbound.rest;

import com.fabriciosanches.domain.port.input.AddItemToOrderCommand;
import com.fabriciosanches.domain.port.input.AddItemToOrderUseCasePort;
import com.fabriciosanches.domain.port.input.CancelOrderCommand;
import com.fabriciosanches.domain.port.input.CancelOrderUseCasePort;
import com.fabriciosanches.domain.port.input.ConfirmOrderCommand;
import com.fabriciosanches.domain.port.input.ConfirmOrderUseCasePort;
import com.fabriciosanches.domain.port.input.CreateOrderCommand;
import com.fabriciosanches.domain.port.input.CreateOrderResult;
import com.fabriciosanches.domain.port.input.CreateOrderUseCasePort;
import com.fabriciosanches.domain.port.input.GetOrderByIdCommand;
import com.fabriciosanches.domain.port.input.GetOrderByIdResult;
import com.fabriciosanches.domain.port.input.GetOrderByIdUseCasePort;
import com.fabriciosanches.domain.port.input.ListOrdersByCustomerCommand;
import com.fabriciosanches.domain.port.input.ListOrdersByCustomerResult;
import com.fabriciosanches.domain.port.input.ListOrdersByCustomerUseCasePort;
import com.fabriciosanches.domain.port.input.RemoveItemFromOrderCommand;
import com.fabriciosanches.domain.port.input.RemoveItemFromOrderUseCasePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Gerenciamento do ciclo de vida dos pedidos")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final CreateOrderUseCasePort createOrderUseCase;
    private final GetOrderByIdUseCasePort getOrderByIdUseCase;
    private final ListOrdersByCustomerUseCasePort listOrdersByCustomerUseCase;
    private final AddItemToOrderUseCasePort addItemToOrderUseCase;
    private final RemoveItemFromOrderUseCasePort removeItemFromOrderUseCase;
    private final ConfirmOrderUseCasePort confirmOrderUseCase;
    private final CancelOrderUseCasePort cancelOrderUseCase;

    public OrderController(CreateOrderUseCasePort createOrderUseCase,
                           GetOrderByIdUseCasePort getOrderByIdUseCase,
                           ListOrdersByCustomerUseCasePort listOrdersByCustomerUseCase,
                           AddItemToOrderUseCasePort addItemToOrderUseCase,
                           RemoveItemFromOrderUseCasePort removeItemFromOrderUseCase,
                           ConfirmOrderUseCasePort confirmOrderUseCase,
                           CancelOrderUseCasePort cancelOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderByIdUseCase = getOrderByIdUseCase;
        this.listOrdersByCustomerUseCase = listOrdersByCustomerUseCase;
        this.addItemToOrderUseCase = addItemToOrderUseCase;
        this.removeItemFromOrderUseCase = removeItemFromOrderUseCase;
        this.confirmOrderUseCase = confirmOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar pedido", description = "Cria um novo pedido para o cliente informado")
    CreateOrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResult result = createOrderUseCase.execute(new CreateOrderCommand(request.customerId()));
        return new CreateOrderResponse(result.orderId());
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Buscar pedido por ID", description = "Retorna os detalhes de um pedido específico")
    GetOrderByIdResult getOrderById(@PathVariable("orderId") String orderId) {
        return getOrderByIdUseCase.execute(new GetOrderByIdCommand(orderId));
    }

    @GetMapping
    @Operation(summary = "Listar pedidos por cliente", description = "Retorna todos os pedidos de um determinado cliente")
    ListOrdersByCustomerResult listOrdersByCustomer(@RequestParam("customerId") @NotBlank String customerId) {
        return listOrdersByCustomerUseCase.execute(new ListOrdersByCustomerCommand(customerId));
    }

    @PostMapping("/{orderId}/items")
    @Operation(summary = "Adicionar item ao pedido", description = "Adiciona um item ao pedido, validando disponibilidade no catálogo")
    GetOrderByIdResult addItem(@PathVariable("orderId") String orderId,
                               @Valid @RequestBody AddItemRequest request) {
        return addItemToOrderUseCase.execute(new AddItemToOrderCommand(
                orderId, request.productId(), request.productName(), request.quantity()));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover item do pedido", description = "Remove um item do pedido (itemId = productId)")
    void removeItem(@PathVariable("orderId") String orderId, @PathVariable("itemId") String itemId) {
        removeItemFromOrderUseCase.execute(new RemoveItemFromOrderCommand(orderId, itemId));
    }

    @PostMapping("/{orderId}/confirm")
    @Operation(summary = "Confirmar pedido", description = "Confirma o pedido (operação idempotente)")
    GetOrderByIdResult confirmOrder(@PathVariable("orderId") String orderId) {
        return confirmOrderUseCase.execute(new ConfirmOrderCommand(orderId));
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancelar pedido", description = "Cancela o pedido com base nas regras do ciclo de vida (operação idempotente)")
    void cancelOrder(@PathVariable("orderId") String orderId) {
        cancelOrderUseCase.execute(new CancelOrderCommand(orderId));
    }

    public record CreateOrderRequest(@NotBlank String customerId) {
    }

    public record CreateOrderResponse(String orderId) {
    }

    public record AddItemRequest(
            @NotBlank String productId,
            @NotBlank String productName,
            @Min(1) int quantity
    ) {
    }
}
