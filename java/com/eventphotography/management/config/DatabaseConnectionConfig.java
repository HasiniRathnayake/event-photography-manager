package com.eventphotography.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class DatabaseConnectionConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionConfig.class);

    @EventListener
    public void handleApplicationFailure(ApplicationFailedEvent event) {
        Throwable exception = event.getException();
        if (exception != null &&
                (exception.getMessage().contains("Login failed") ||
                        exception.getMessage().contains("SQLServerException") ||
                        exception.getMessage().contains("Unable to open JDBC Connection"))) {

            logger.error("🔴 DATABASE CONNECTION FAILED!");
            logger.error("═══════════════════════════════════════════════════════════");
            logger.error("❌ SQL Server authentication failed for EventPhotographyDB");
            logger.error("📋 SOLUTIONS:");
            logger.error("1. SQL SERVER AUTHENTICATION (Current config):");
            logger.error("   - Make sure SQL Server allows SQL Server Authentication");
            logger.error("   - Verify username/password in application.properties");
            logger.error("   - Current config: sa/789");
            logger.error("");
            logger.error("2. WINDOWS AUTHENTICATION:");
            logger.error("   - Requires sqljdbc_auth.dll in system PATH");
            logger.error("   - Set integratedSecurity=true in connection string");
            logger.error("");
            logger.error("3. CHECK SQL SERVER STATUS:");
            logger.error("   - Ensure SQL Server service is running");
            logger.error("   - Verify EventPhotographyDB database exists");
            logger.error("   - Check if SQL Server is listening on port 1433");
            logger.error("═══════════════════════════════════════════════════════════");
        }
    }
}