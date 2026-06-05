package com.ceodashboard.backend.hrms.config;

import com.ceodashboard.backend.hrms.exception.*;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(FeignErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        logger.error("Feign client error for method: {}, status: {}", methodKey, response.status());

        switch (response.status()) {
            case 400:
                return new IllegalArgumentException("Bad request to HRMS API: " + response.reason());
            case 401:
                return new UnauthorizedException("Unauthorized access to HRMS API. Check your credentials.");
            case 403:
                return new ForbiddenException("Forbidden access to HRMS API. Insufficient permissions.");
            case 404:
                return new EmployeeNotFoundException("Resource not found in HRMS API: " + response.reason());
            case 500:
            case 502:
            case 503:
                return new HrmsApiServerException("HRMS API server error: " + response.reason(), response.status());
            case 504:
                return new HrmsApiTimeoutException(methodKey);
            default:
                return new HrmsApiException(
                    "HRMS API error: " + response.reason(),
                    response.status(),
                    "HRMS_API_ERROR"
                );
        }
    }
}
