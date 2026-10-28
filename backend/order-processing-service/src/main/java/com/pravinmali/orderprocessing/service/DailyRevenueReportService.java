package com.pravinmali.orderprocessing.service;

import com.pravinmali.orderprocessing.dto.response.DailyRevenueReportResponse;
import com.pravinmali.orderprocessing.dto.response.DailyRevenueSummary;
import com.pravinmali.orderprocessing.entity.OrderStatus;
import com.pravinmali.orderprocessing.repository.OrderRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for computing daily revenue summaries grouped by {@link OrderStatus}
 * and exporting them as RFC 4180 CSV files.
 */
@Service
public class DailyRevenueReportService {

    private static final Logger log = LoggerFactory.getLogger(DailyRevenueReportService.class);

    private static final String[] CSV_HEADERS = {
            "Date", "Order Status", "Order Count", "Subtotal (INR)", "Tax Amount (INR)", "Total Revenue (INR)"
    };

    private final OrderRepository orderRepository;

    @Value("${app.scheduling.revenue-export-dir:exports/revenue-reports}")
    private String exportDir;

    public DailyRevenueReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Computes the daily revenue summary report for the given date.
     * Defaults to today if date is null.
     */
    @Transactional(readOnly = true)
    public DailyRevenueReportResponse getDailyRevenueReport(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<DailyRevenueSummary> rawList = orderRepository.findDailyRevenueGroupedByStatus(startOfDay, endOfDay);
        List<DailyRevenueSummary> fullBreakdown = completeStatusBreakdown(rawList);

        long totalOrders = 0;
        BigDecimal totalSubtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (DailyRevenueSummary s : fullBreakdown) {
            totalOrders += s.orderCount();
            totalSubtotal = totalSubtotal.add(s.totalSubtotal());
            totalTax = totalTax.add(s.totalTax());
            totalRevenue = totalRevenue.add(s.totalRevenue());
        }

        return new DailyRevenueReportResponse(
                targetDate,
                totalOrders,
                totalSubtotal,
                totalTax,
                totalRevenue,
                fullBreakdown
        );
    }

    /**
     * Serializes the revenue summary for the given date into CSV bytes.
     */
    @Transactional(readOnly = true)
    public byte[] generateDailyRevenueCsv(LocalDate date) throws IOException {
        DailyRevenueReportResponse report = getDailyRevenueReport(date);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.RFC4180
                     .builder()
                     .setHeader(CSV_HEADERS)
                     .build())) {

            for (DailyRevenueSummary item : report.breakdown()) {
                printer.printRecord(
                        report.date(),
                        item.status().name(),
                        item.orderCount(),
                        item.totalSubtotal().toPlainString(),
                        item.totalTax().toPlainString(),
                        item.totalRevenue().toPlainString()
                );
            }

            // Print summary / total row
            printer.printRecord(
                    report.date(),
                    "TOTAL / SUMMARY",
                    report.totalOrders(),
                    report.totalSubtotal().toPlainString(),
                    report.totalTax().toPlainString(),
                    report.totalRevenue().toPlainString()
            );
        }

        return out.toByteArray();
    }

    /**
     * Generates and writes the daily revenue summary CSV file to the designated export directory.
     *
     * @param date target date
     * @return Path to the created file
     * @throws IOException on file write error
     */
    public Path exportAndSaveDailyRevenueCsv(LocalDate date) throws IOException {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        byte[] csvData = generateDailyRevenueCsv(targetDate);

        Path dirPath = Paths.get(exportDir);
        Files.createDirectories(dirPath);

        String filename = "daily-revenue-" + targetDate + ".csv";
        Path targetFilePath = dirPath.resolve(filename);
        Files.write(targetFilePath, csvData);

        log.info("[REVENUE REPORT] Generated CSV report for {} -> {}", targetDate, targetFilePath.toAbsolutePath());
        return targetFilePath;
    }

    /**
     * Ensures all 5 OrderStatus values are represented in the list (with zero counts/revenue if none existed).
     */
    private List<DailyRevenueSummary> completeStatusBreakdown(List<DailyRevenueSummary> rawList) {
        Map<OrderStatus, DailyRevenueSummary> map = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            map.put(status, new DailyRevenueSummary(status, 0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        }

        if (rawList != null) {
            for (DailyRevenueSummary s : rawList) {
                if (s.status() != null) {
                    map.put(s.status(), s);
                }
            }
        }

        return new ArrayList<>(map.values());
    }
}
