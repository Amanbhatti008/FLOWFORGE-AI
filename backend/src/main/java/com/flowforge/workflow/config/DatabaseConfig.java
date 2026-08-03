package com.flowforge.workflow.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:}")
    private String springDatasourceUrl;

    @Value("${spring.datasource.username:postgres}")
    private String username;

    @Value("${spring.datasource.password:postgres}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        String finalUrl = System.getenv("SPRING_DATASOURCE_URL");
        
        if (finalUrl == null || finalUrl.trim().isEmpty()) {
            finalUrl = System.getenv("DATABASE_URL");
        }
        
        if (finalUrl == null || finalUrl.trim().isEmpty()) {
            finalUrl = springDatasourceUrl;
        }

        if (finalUrl == null || finalUrl.trim().isEmpty()) {
            // Throw a very specific error so the user knows exactly what failed
            throw new IllegalArgumentException("FATAL CONFIG ERROR: The SPRING_DATASOURCE_URL is completely empty. Please make sure you have entered the Value in the Render dashboard correctly!");
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(finalUrl);
        
        // Check for username in env variables directly too, just in case
        String finalUsername = System.getenv("DB_USER");
        if (finalUsername == null || finalUsername.trim().isEmpty()) {
            finalUsername = username;
        }
        dataSource.setUsername(finalUsername);
        
        String finalPassword = System.getenv("DB_PASSWORD");
        if (finalPassword == null || finalPassword.trim().isEmpty()) {
            finalPassword = password;
        }
        dataSource.setPassword(finalPassword);
        
        dataSource.setDriverClassName(driverClassName);
        dataSource.setMaximumPoolSize(20);
        dataSource.setMinimumIdle(5);
        
        return dataSource;
    }
}
