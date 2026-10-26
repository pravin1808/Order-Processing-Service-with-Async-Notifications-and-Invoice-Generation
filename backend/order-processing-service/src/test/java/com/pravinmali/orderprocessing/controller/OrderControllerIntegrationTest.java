package com.pravinmali.orderprocessing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pravinmali.orderprocessing.dto.request.CreateOrderRequest;
import com.pravinmali.orderprocessing.dto.request.OrderItemRequest;
import com.pravinmali.orderprocessing.dto.request.UpdateOrderStatusRequest;
import com.pravinmali.orderprocessing.dto.response.OrderResponse;
import com.pravinmali.orderprocessing.entity.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests using H2 in-memory database.
 *
 * <p>Each test runs in a transaction that is rolled back on completion,
 * so tests are fully isolated with no DB cleanup needed.</p>
 *
 * <p>Uses the "test" profile which activates {@code application-test.properties}
 * (H2, create-drop DDL, disabled SpringDoc).</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/orders";

    // =========================================================================
    // Helper
    // =========================================================================

    private CreateOrderRequest validCreateRequest() {
        return new CreateOrderRequest(
                "Pravin Mali",
                "pravin@example.com",
                "9876543210",
                "123, Pune, Maharashtra, India",
                List.of(
                        new OrderItemRequest("Laptop", 1, new BigDecimal("50000.00")),
                        new OrderItemRequest("Mouse", 2, new BigDecimal("500.00"))
                )
        );
    }

    /** Creates an order and returns its deserialized response. */
    private OrderResponse createOrder() throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), OrderResponse.class);
    }

    // =========================================================================
    // POST /api/v1/orders — create order
    // =========================================================================

    @Test
    @DisplayName("POST /orders: valid request → 201 with correct totals")
    void createOrder_validRequest_returns201WithTotals() throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.customer.email").value("pravin@example.com"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andReturn();

        OrderResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), OrderResponse.class);

        assertThat(response.subtotal()).isEqualByComparingTo("51000.00");
        assertThat(response.taxAmount()).isEqualByComparingTo("9180.00");
        assertThat(response.total()).isEqualByComparingTo("60180.00");
    }

    @Test
    @DisplayName("POST /orders: empty items list → 400 with validation error")
    void createOrder_emptyItems_returns400() throws Exception {
        CreateOrderRequest bad = new CreateOrderRequest(
                "Pravin Mali", "pravin@example.com", null, "Pune", List.of());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("POST /orders: invalid email → 400 with field error on customerEmail")
    void createOrder_invalidEmail_returns400() throws Exception {
        CreateOrderRequest bad = new CreateOrderRequest(
                "Pravin", "not-an-email", null, "Pune",
                List.of(new OrderItemRequest("Item", 1, new BigDecimal("100"))));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customerEmail"));
    }

    // =========================================================================
    // GET /api/v1/orders/{id} — get by ID
    // =========================================================================

    @Test
    @DisplayName("GET /orders/{id}: existing order → 200 with full detail")
    void getOrderById_existingOrder_returns200() throws Exception {
        OrderResponse created = createOrder();

        mockMvc.perform(get(BASE_URL + "/" + created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id().toString()))
                .andExpect(jsonPath("$.customer.name").value("Pravin Mali"))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    @DisplayName("GET /orders/{id}: non-existent id → 404")
    void getOrderById_notFound_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // =========================================================================
    // GET /api/v1/orders — list with pagination
    // =========================================================================

    @Test
    @DisplayName("GET /orders: returns paginated list")
    void listOrders_returnsPaginatedList() throws Exception {
        createOrder();
        createOrder();

        mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].customerEmail").value("pravin@example.com"));
    }

    // =========================================================================
    // PATCH /api/v1/orders/{id}/status — update status
    // =========================================================================

    @Test
    @DisplayName("PATCH /orders/{id}/status: PENDING → CONFIRMED → 200")
    void updateStatus_validTransition_returns200() throws Exception {
        OrderResponse created = createOrder();
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

        mockMvc.perform(patch(BASE_URL + "/" + created.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PATCH /orders/{id}/status: SHIPPED → PENDING → 422 Unprocessable")
    void updateStatus_invalidTransition_returns422() throws Exception {
        OrderResponse created = createOrder();

        // PENDING → CONFIRMED
        mockMvc.perform(patch(BASE_URL + "/" + created.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateOrderStatusRequest(OrderStatus.CONFIRMED))))
                .andExpect(status().isOk());

        // CONFIRMED → SHIPPED
        mockMvc.perform(patch(BASE_URL + "/" + created.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateOrderStatusRequest(OrderStatus.SHIPPED))))
                .andExpect(status().isOk());

        // SHIPPED → PENDING (illegal)
        mockMvc.perform(patch(BASE_URL + "/" + created.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateOrderStatusRequest(OrderStatus.PENDING))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    // =========================================================================
    // GET /api/v1/orders/{id}/invoice — PDF download
    // =========================================================================

    @Test
    @DisplayName("GET /orders/{id}/invoice: CONFIRMED order → 200 application/pdf")
    void downloadInvoice_confirmedOrder_returnsPdf() throws Exception {
        OrderResponse created = createOrder();

        // Move to CONFIRMED
        mockMvc.perform(patch(BASE_URL + "/" + created.id() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateOrderStatusRequest(OrderStatus.CONFIRMED))))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL + "/" + created.id() + "/invoice"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("invoice-" + created.id())));
    }

    @Test
    @DisplayName("GET /orders/{id}/invoice: PENDING order → 409 Conflict")
    void downloadInvoice_pendingOrder_returns409() throws Exception {
        OrderResponse created = createOrder();

        mockMvc.perform(get(BASE_URL + "/" + created.id() + "/invoice"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
