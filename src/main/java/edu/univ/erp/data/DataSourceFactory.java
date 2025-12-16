package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;

public final class DataSourceFactory {

    private static final Logger log = LoggerFactory.getLogger(DataSourceFactory.class);

    private DataSourceFactory() {
    }

    public static DataSource fromProperties(Properties properties, String prefix) {
        Objects.requireNonNull(properties, "properties");
        Objects.requireNonNull(prefix, "prefix");

        String jdbcUrl = properties.getProperty(prefix + ".jdbcUrl");
        String username = properties.getProperty(prefix + ".username");
        String password = properties.getProperty(prefix + ".password");

        if (jdbcUrl == null) {
            throw new IllegalArgumentException("Missing JDBC URL for prefix " + prefix);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty(prefix + ".maxPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty(prefix + ".minIdle", "2")));
        config.setPoolName(prefix + "-pool");
        config.setAutoCommit(false);

        String schema = properties.getProperty(prefix + ".schema");
        if (schema != null && !schema.isBlank()) {
            config.setSchema(schema);
        }

        log.info("Creating datasource for {}", prefix);
        return new HikariDataSource(config);
    }
}

