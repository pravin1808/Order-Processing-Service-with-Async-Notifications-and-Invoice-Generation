package com.pravinmali.orderprocessing.controller;

import com.pravinmali.orderprocessing.service.CsvExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

/**
 * REST controller for data export operations.
 */
@RestController
@RequestMapping("/api/v1/export")
@Tag(name = "Export", description = "Data export endpoints")
public class ExportController {

    private final CsvExportService csvExportService;

    public ExportController(CsvExportService csvExportService) {
        this.csvExportService = csvExportService;
    }

    /**
     * Export all orders as a downloadable CSV file.
     * Filename includes today's date for easy archiving: orders-2026-10-26.csv
     */
    @GetMapping("/orders/csv")
    @Operation(summary = "Export all orders as CSV")
    public ResponseEntity<byte[]> exportOrdersCsv() throws IOException {
        byte[] csv = csvExportService.exportAllOrders();
        String filename = "orders-" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(csv);
    }
}
