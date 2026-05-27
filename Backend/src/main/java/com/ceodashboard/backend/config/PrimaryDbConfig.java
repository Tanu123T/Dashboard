package com.ceodashboard.backend.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
        "com.ceodashboard.backend.repository" }, entityManagerFactoryRef = "primaryEntityManagerFactory", transactionManagerRef = "primaryTransactionManager")
public class PrimaryDbConfig {

    @Value("${spring.datasource.url:jdbc:postgresql://ep-tiny-mode-aokib5ac.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require}")
    private String dbUrl;

    @Value("${spring.datasource.username:neondb_owner}")
    private String dbUsername;

    @Value("${spring.datasource.password:npg_HSG6v2dQzVfr}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String dbDriver;

    @Primary
    @Bean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        if (dbUrl == null || dbUrl.isEmpty()) {
            throw new IllegalStateException(
                    "Database URL is not configured. Please set spring.datasource.url in application.properties");
        }
        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .driverClassName(dbDriver)
                .build();
    }

    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            @Qualifier("primaryDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.ceodashboard.backend.entity");
        em.setPersistenceUnitName("primary");

        org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter vendorAdapter = new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        java.util.Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Primary
    @Bean(name = "primaryTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory) {
        return new JpaTransactionManager(primaryEntityManagerFactory.getObject());
    }
}
