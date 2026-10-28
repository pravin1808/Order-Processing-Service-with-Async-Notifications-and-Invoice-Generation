package com.pravinmali.orderprocessing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's scheduled task execution capability.
 * Used for automated background jobs like the nightly revenue CSV export.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
