package com.blueant_crm_erp.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    /**
     * Binds spring.datasource.* from application.yml into DataSourceProperties
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Creates HikariCP connection pool using above properties
     * HikariCP is the fastest Java connection pool — Spring Boot default
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        String url = properties.getUrl();
        if (url != null && url.startsWith("mysql://")) {
            try {
                URI uri = new URI(url);
                String userInfo = uri.getUserInfo();
                if (userInfo != null) {
                    int colonIndex = userInfo.indexOf(':');
                    if (colonIndex != -1) {
                        properties.setUsername(userInfo.substring(0, colonIndex));
                        properties.setPassword(userInfo.substring(colonIndex + 1));
                    } else {
                        properties.setUsername(userInfo);
                    }
                }

                String host = uri.getHost();
                int port = uri.getPort();
                String path = uri.getPath();
                String query = uri.getQuery();

                StringBuilder jdbcUrl = new StringBuilder("jdbc:mysql://").append(host);
                if (port != -1) {
                    jdbcUrl.append(":").append(port);
                }
                if (path != null) {
                    jdbcUrl.append(path);
                }
                if (query != null) {
                    jdbcUrl.append("?").append(query);
                }

                String fixedUrl = addTimezoneParameters(jdbcUrl.toString());
                logger.info("Parsed mysql:// URL. Clean JDBC URL: {}", sanitizeUrl(fixedUrl));
                properties.setUrl(fixedUrl);
            } catch (URISyntaxException e) {
                logger.error("Failed to parse mysql:// database URL: {}", url, e);
                // Fallback
                properties.setUrl(addTimezoneParameters("jdbc:" + url));
            }
        } else if (url != null) {
            String fixedUrl = addTimezoneParameters(url);
            logger.info("Using database URL: {}", sanitizeUrl(fixedUrl));
            properties.setUrl(fixedUrl);
        }
        return properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    private String addTimezoneParameters(String url) {
        if (url == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(url);
        if (!url.contains("?")) {
            sb.append("?");
        } else if (!url.endsWith("&") && !url.endsWith("?")) {
            sb.append("&");
        }
        
        if (!url.contains("connectionTimeZone=")) {
            sb.append("connectionTimeZone=%2B05:30&");
        }
        if (!url.contains("forceConnectionTimeZoneToSession=")) {
            sb.append("forceConnectionTimeZoneToSession=true&");
        }
        if (!url.contains("serverTimezone=")) {
            sb.append("serverTimezone=%2B05:30&");
        }
        
        String result = sb.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String sanitizeUrl(String url) {
        if (url == null) {
            return "null";
        }
        return url.replaceAll(":[^:@/]+@", ":****@");
    }
}
