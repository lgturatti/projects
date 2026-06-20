package com.fabriciosanches.integration;

import com.fabriciosanches.adapter.outbound.http.CatalogClientAdapter;
import com.fabriciosanches.adapter.outbound.http.CustomerClientAdapter;
import com.fabriciosanches.domain.exception.DomainException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("integration")
@DisplayName("Order Service - Testes de Integração")
/*
 * NOTE: This integration test requires Docker to be installed and running.
 * It will be automatically skipped if Docker is not available.
 * To run: mvn clean verify -DskipITs=false
 * To skip: mvn clean test
 */
class OrderServiceIntegrationIT {

    private static final String JWT_SECRET = "integration-test-secret-hs256-key-1";

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("orderservice")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    static GenericContainer<?> wireMock = new GenericContainer<>("wiremock/wiremock:3.5.4")
            .withExposedPorts(8080)
            .withCopyFileToContainer(
                    MountableFile.forHostPath("wiremock/mappings"),
                    "/home/wiremock/mappings")
            .waitingFor(Wait.forHttp("/__admin/mappings").forStatusCode(200));

    @DynamicPropertySource
    static void configureWireMockProperties(DynamicPropertyRegistry registry) {
        String wireMockBaseUrl = "http://localhost:" + wireMock.getMappedPort(8080);
        registry.add("clients.customer.base-url", () -> wireMockBaseUrl);
        registry.add("clients.catalog.base-url", () -> wireMockBaseUrl);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerClientAdapter customerClientAdapter;

    @Autowired
    private CatalogClientAdapter catalogClientAdapter;

    // ─────────────────────────────────────────────────────────────────────────
    // REST API — Segurança
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve retornar 401 ao criar pedido sem token JWT")
    void shouldReturn401WhenCreatingOrderWithoutToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"customerId\":\"customer-active-001\"}", headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/orders", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Deve retornar 403 ao criar pedido com escopo incorreto")
    void shouldReturn403WhenCreatingOrderWithWrongScope() {
        String token = createJwt("orders:read");
        HttpHeaders headers = buildAuthHeaders(token);
        HttpEntity<String> request = new HttpEntity<>("{\"customerId\":\"customer-active-001\"}", headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/orders", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Deve criar pedido com sucesso ao fornecer JWT com escopo orders:write")
    void shouldCreateOrderSuccessfullyWithValidJwt() {
        String token = createJwt("orders:write");
        HttpHeaders headers = buildAuthHeaders(token);
        HttpEntity<String> request = new HttpEntity<>("{\"customerId\":\"customer-active-001\"}", headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/orders", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("orderId");
    }

    @Test
    @DisplayName("Deve retornar 400 para payload inválido ao criar pedido")
    void shouldReturn400WhenCreatingOrderWithInvalidPayload() {
        String token = createJwt("orders:write");
        HttpHeaders headers = buildAuthHeaders(token);
        HttpEntity<String> request = new HttpEntity<>("{\"customerId\":\"\"}", headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/orders", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Deve processar pagamento com sucesso ao fornecer JWT com escopo payments:write")
    void shouldProcessPaymentSuccessfullyWithValidJwt() {
        String token = createJwt("payments:write");
        HttpHeaders headers = buildAuthHeaders(token);
        HttpEntity<String> request = new HttpEntity<>("{\"orderId\":\"order-001\",\"paymentMethod\":\"pix\"}", headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/payments", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).contains("paymentId");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CustomerClientAdapter — Integração com WireMock
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CustomerClientAdapter: deve validar cliente ativo sem lançar exceção")
    void shouldValidateActiveCustomerSuccessfully() {
        assertDoesNotThrow(() -> customerClientAdapter.validateCustomer("customer-active-001"));
    }

    @Test
    @DisplayName("CustomerClientAdapter: deve lançar DomainException para cliente bloqueado (422)")
    void shouldThrowDomainExceptionForBlockedCustomer() {
        assertThatThrownBy(() -> customerClientAdapter.validateCustomer("customer-blocked-001"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("inválido ou inativo");
    }

    @Test
    @DisplayName("CustomerClientAdapter: deve lançar DomainException para cliente não encontrado (404)")
    void shouldThrowDomainExceptionForNotFoundCustomer() {
        assertThatThrownBy(() -> customerClientAdapter.validateCustomer("customer-not-found-001"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("inválido ou inativo");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CatalogClientAdapter — Integração com WireMock
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CatalogClientAdapter: deve retornar preço do produto com sucesso")
    void shouldReturnProductPriceSuccessfully() {
        BigDecimal price = catalogClientAdapter.getProductPrice("product-001");

        assertThat(price).isNotNull();
        assertThat(price).isEqualByComparingTo(new BigDecimal("19.99"));
    }

    @Test
    @DisplayName("CatalogClientAdapter: deve validar estoque disponível sem lançar exceção")
    void shouldValidateAvailableStockSuccessfully() {
        assertDoesNotThrow(() -> catalogClientAdapter.validateStock("product-001", 5));
    }

    @Test
    @DisplayName("CatalogClientAdapter: deve lançar DomainException para produto sem estoque (422)")
    void shouldThrowDomainExceptionForInsufficientStock() {
        assertThatThrownBy(() -> catalogClientAdapter.validateStock("product-out-of-stock-001", 1))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    @DisplayName("CatalogClientAdapter: deve lançar DomainException para produto não encontrado no catálogo (404)")
    void shouldThrowDomainExceptionForProductNotFoundInCatalog() {
        assertThatThrownBy(() -> catalogClientAdapter.getProductPrice("product-not-found-001"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String createJwt(String... scopes) {
        try {
            SecretKey key = new SecretKeySpec(
                    JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            JWSSigner signer = new MACSigner(key);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject("test-user")
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + 3_600_000L))
                    .claim("scope", String.join(" ", scopes))
                    .build();

            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao criar JWT de teste", e);
        }
    }

    private HttpHeaders buildAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
}
