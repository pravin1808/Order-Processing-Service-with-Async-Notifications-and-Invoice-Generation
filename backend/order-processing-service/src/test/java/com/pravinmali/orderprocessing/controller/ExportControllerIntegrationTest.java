package com.pravinmali.orderprocessing.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/export/orders/csv: ADMIN -> returns 200 with CSV")
    void exportOrdersCsv_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/export/orders/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"orders-")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/export/revenue/csv: ADMIN -> returns 200 with daily revenue CSV")
    void exportDailyRevenueCsv_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/export/revenue/csv")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"daily-revenue-")))
                .andExpect(content().string(containsString("TOTAL / SUMMARY")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/export/revenue/summary: ADMIN -> returns 200 with breakdown JSON")
    void getDailyRevenueSummary_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/export/revenue/summary")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.totalOrders").exists())
                .andExpect(jsonPath("$.breakdown").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/export/revenue/trigger: ADMIN -> triggers nightly job successfully")
    void triggerNightlyRevenueJob_asAdmin_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/export/revenue/trigger")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.filePath").exists());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /api/v1/export/revenue/summary: CUSTOMER -> returns 403 Forbidden")
    void getRevenueSummary_asCustomer_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/export/revenue/summary"))
                .andExpect(status().isForbidden());
    }
}
