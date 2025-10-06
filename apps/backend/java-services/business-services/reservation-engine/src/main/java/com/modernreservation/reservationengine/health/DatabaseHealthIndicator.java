package com.modernreservation.reservationengine.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Custom health indicator for database connectivity
 * Provides detailed health information about PostgreSQL connection
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT version()")) {

            if (resultSet.next()) {
                String version = resultSet.getString(1);
                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("version", version)
                        .withDetail("validationQuery", "SELECT version()")
                        .build();
            }

            return Health.unknown()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("reason", "No result from validation query")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", e.getClass().getSimpleName())
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }
}
