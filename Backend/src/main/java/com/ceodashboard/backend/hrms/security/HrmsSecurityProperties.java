package com.ceodashboard.backend.hrms.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hrms")
@Data
@NoArgsConstructor
public class HrmsSecurityProperties {

    @NestedConfigurationProperty
    private Api api = new Api();

    @NestedConfigurationProperty
    private Token token = new Token();

    @NestedConfigurationProperty
    private Oauth oauth = new Oauth();

    private Integer companyId = 1;

    @Data
    @NoArgsConstructor
    public static class Api {
        private String baseUrl;
        private String authHeader;
    }

    @Data
    @NoArgsConstructor
    public static class Token {
        private String endpoint;
    }

    @Data
    @NoArgsConstructor
    public static class Oauth {
        private String clientId;
        private String username;
        private String password;
        private String scope;
    }
}
