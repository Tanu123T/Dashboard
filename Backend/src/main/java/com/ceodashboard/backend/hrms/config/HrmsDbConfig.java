package com.ceodashboard.backend.hrms.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"com.ceodashboard.backend.hrms.repository"},
        entityManagerFactoryRef = "hrmsEntityManagerFactory",
        transactionManagerRef = "hrmsTransactionManager"
)
public class HrmsDbConfig {

    @Bean(name = "hrmsDataSource")
    @ConfigurationProperties(prefix = "hrms.datasource")
    public DataSource hrmsDataSource() {
        return DataSourceBuilder.create().build();
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
