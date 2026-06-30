package com.travel.expense_management.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Checking and adjusting database check constraints for TripStatus enum values...");
            // Drop the old check constraint that restricts values to PENDING, APPROVED, REJECTED
            jdbcTemplate.execute("ALTER TABLE trips DROP CONSTRAINT IF EXISTS trips_status_check");
            log.info("Successfully dropped the trips_status_check constraint to support the REIMBURSED status.");
        } catch (Exception e) {
            log.error("Failed to alter trips status check constraint: {}", e.getMessage());
        }
    }
}
