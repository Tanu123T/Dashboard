package com.ceodashboard.backend.hrms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
        "com.ceodashboard.backend.hrms.repository" }, entityManagerFactoryRef = "hrmsEntityManagerFactory", transactionManagerRef = "hrmsTransactionManager")
public class HrmsDbConfig {

    private static final Logger logger = LoggerFactory.getLogger(HrmsDbConfig.class);

    @Value("${hrms.datasource.jdbc-url:jdbc:mysql://167.235.246.36:3307/smarthrms?useSSL=false&allowPublicKeyRetrieval=true}")
    private String hrmsUrl;

    @Value("${hrms.datasource.username:readonly_hrms}")
    private String hrmsUsername;

    @Value("${hrms.datasource.password:VGhrms@Read}")
    private String hrmsPassword;

    @Bean(name = "hrmsDataSource")
    public DataSource hrmsDataSource() {
        if (hrmsUrl == null || hrmsUrl.isEmpty()) {
            logger.warn("HRMS JDBC URL is not configured. HRMS datasource will not be available.");
            return createDummyDataSource();
        }

        try {
            DataSource dataSource = DataSourceBuilder.create()
                    .url(hrmsUrl)
                    .username(hrmsUsername)
                    .password(hrmsPassword)
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .type(HikariDataSource.class)
                    .build();

            logger.info("HRMS datasource created successfully for URL: {}", hrmsUrl);
            return dataSource;
        } catch (Exception e) {
            logger.error("Failed to create HRMS datasource. Using dummy datasource. Error: {}", e.getMessage());
            return createDummyDataSource();
        }
    }

    private DataSource createDummyDataSource() {
        logger.warn("Creating dummy H2 datasource for HRMS. This is a fallback when MySQL is unavailable.");
        return DataSourceBuilder.create()
                .url("jdbc:h2:mem:hrms_dummy")
                .driverClassName("org.h2.Driver")
                .username("sa")
                .password("")
                .build();
    }

    @Bean(name = "hrmsJdbcTemplate")
    public JdbcTemplate hrmsJdbcTemplate(@Qualifier("hrmsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "hrmsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean hrmsEntityManagerFactory(
            @Qualifier("hrmsDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.ceodashboard.backend.hrms.entity");
        em.setPersistenceUnitName("hrms");

        org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter vendorAdapter = new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "hrmsTransactionManager")
    public PlatformTransactionManager hrmsTransactionManager(
            @Qualifier("hrmsEntityManagerFactory") LocalContainerEntityManagerFactoryBean hrmsEntityManagerFactory) {
        return new JpaTransactionManager(hrmsEntityManagerFactory.getObject());
    }
}
