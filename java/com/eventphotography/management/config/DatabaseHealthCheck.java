package com.eventphotography.management.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabaseHealthCheck {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthCheck.class);

    @Autowired
    private DataSource dataSource;

    @EventListener(ApplicationReadyEvent.class)
    public void checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            logger.info("✅ Database connection successful!");
            logger.info("Database URL: {}", connection.getMetaData().getURL());
            logger.info("Database Product: {}", connection.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            logger.error("❌ Database connection failed: {}", e.getMessage());
            logger.error("Please check if SQL Server is running and the EventPhotographyDB database exists");
            logger.error("Connection URL: jdbc:sqlserver://localhost:1433;databaseName=EventPhotographyDB");
        }
    }
}