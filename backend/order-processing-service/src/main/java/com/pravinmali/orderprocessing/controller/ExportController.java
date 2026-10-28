package com.pravinmali.orderprocessing.controller;

import com.pravinmali.orderprocessing.dto.response.DailyRevenueReportResponse;
import com.pravinmali.orderprocessing.job.DailyRevenueExportJob;
import com.pravinmali.orderprocessing.service.CsvExportService;
import com.pravinmali.orderprocessing.service.DailyRevenueReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller for data export operations.
 */
@RestController
@RequestMapping("/api/v1/export")
@Tag(name = "Export", description = "Data export endpoints")
public class ExportController {

    private final CsvExportService csvExportService;
    private final DailyRevenueReportService dailyRevenueReportService;
    private final DailyRevenueExportJob dailyRevenueExportJob;

    public ExportController(CsvExportService csvExportService,
                            DailyRevenueReportService dailyRevenueReportService,
                            DailyRevenueExportJob dailyRevenueExportJob) {
        this.csvExportService = csvExportService;
        this.dailyRevenueReportService = dailyRevenueReportService;
        this.dailyRevenueExportJob = dailyRevenueExportJob;
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

    /**
     * Export daily revenue summary grouped by order status as a downloadable CSV file.
     */
    @GetMapping("/revenue/csv")
    @Operation(summary = "Download daily revenue CSV summary grouped by order status")
    public ResponseEntity<byte[]> exportDailyRevenueCsv(
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws IOException {

        LocalDate targetDate = date != null ? date : LocalDate.now();
        byte[] csvBytes = dailyRevenueReportService.generateDailyRevenueCsv(targetDate);
        String filename = "daily-revenue-" + targetDate + ".csv";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(csvBytes);
    }

    /**
     * Retrieve daily revenue summary report JSON for dashboard display.
     */
    @GetMapping("/revenue/summary")
    @Operation(summary = "Get daily revenue summary report grouped by order status")
    public ResponseEntity<DailyRevenueReportResponse> getDailyRevenueSummary(
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        DailyRevenueReportResponse report = dailyRevenueReportService.getDailyRevenueReport(date);
        return ResponseEntity.ok(report);
    }

    /**
     * Manually triggers the nightly revenue export job for a given date (or today).
     */
    @PostMapping("/revenue/trigger")
    @Operation(summary = "Manually trigger the nightly revenue CSV export job")
    public ResponseEntity<Map<String, Object>> triggerNightlyRevenueJob(
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws Exception {

        LocalDate targetDate = date != null ? date : LocalDate.now();
        Path filePath = dailyRevenueExportJob.triggerManually(targetDate);
        DailyRevenueReportResponse report = dailyRevenueReportService.getDailyRevenueReport(targetDate);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Nightly revenue summary job executed successfully for " + targetDate,
                "date", targetDate.toString(),
                "filePath", filePath.toString(),
                "totalOrders", report.totalOrders(),
                "totalRevenue", report.totalRevenue(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
