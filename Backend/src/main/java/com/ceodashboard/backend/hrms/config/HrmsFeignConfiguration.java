package com.ceodashboard.backend.hrms.config;

import feign.Request;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class HrmsFeignConfiguration {

    @Bean
    public Request.Options feignRequestOptions(
            @Value("${feign.client.config.default.connectTimeout:5000}") int connectTimeoutMillis,
            @Value("${feign.client.config.default.readTimeout:10000}") int readTimeoutMillis) {
        return new Request.Options(connectTimeoutMillis, TimeUnit.MILLISECONDS, readTimeoutMillis, TimeUnit.MILLISECONDS, true);
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, 2000, 3);
    }
}
