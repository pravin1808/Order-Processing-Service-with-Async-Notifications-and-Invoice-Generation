package com.pravinmali.orderprocessing.job;

import com.pravinmali.orderprocessing.dto.response.DailyRevenueReportResponse;
import com.pravinmali.orderprocessing.service.DailyRevenueReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Nightly scheduled job that generates and exports a daily revenue summary CSV
 * grouped by {@link com.pravinmali.orderprocessing.entity.OrderStatus}.
 *
 * <p>Default schedule: every night at midnight (00:00:00).
 * Configurable via {@code app.scheduling.nightly-cron}.</p>
 */
@Component
public class DailyRevenueExportJob {

    private static final Logger log = LoggerFactory.getLogger(DailyRevenueExportJob.class);

    private final DailyRevenueReportService dailyRevenueReportService;

    public DailyRevenueExportJob(DailyRevenueReportService dailyRevenueReportService) {
        this.dailyRevenueReportService = dailyRevenueReportService;
    }

    /**
     * Executes automatically every midnight to generate the daily revenue CSV report
     * for the preceding day's business.
     */
    @Scheduled(cron = "${app.scheduling.nightly-cron:0 0 0 * * *}")
    public void executeNightlyRevenueExport() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("[NIGHTLY JOB] Starting nightly daily revenue summary export for date={}", yesterday);

        try {
            Path file = dailyRevenueReportService.exportAndSaveDailyRevenueCsv(yesterday);
            DailyRevenueReportResponse summary = dailyRevenueReportService.getDailyRevenueReport(yesterday);

            log.info("[NIGHTLY JOB] Completed successfully: date={}, totalOrders={}, totalRevenue=INR {}, file={}",
                    yesterday, summary.totalOrders(), summary.totalRevenue(), file.toAbsolutePath());
        } catch (Exception e) {
            log.error("[NIGHTLY JOB] Failed to export daily revenue summary for date={}: {}", yesterday, e.getMessage(), e);
        }
    }

    /**
     * Manually triggers the export job for a specific date (or today if null).
     * Useful for admin manual execution, integration testing, and demoing.
     *
     * @param date target date
     * @return Path to generated CSV file
     * @throws Exception if generation fails
     */
    public Path triggerManually(LocalDate date) throws Exception {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        log.info("[MANUAL TRIGGER] Triggering revenue export job for date={}", targetDate);
        return dailyRevenueReportService.exportAndSaveDailyRevenueCsv(targetDate);
    }
}
