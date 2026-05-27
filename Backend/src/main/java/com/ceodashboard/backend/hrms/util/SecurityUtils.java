package com.ceodashboard.backend.hrms.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SecurityUtils {

    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Jwt jwt = jwtToken.getToken();
            return jwt.getClaimAsString("email"); // Fallback to preferred_username if needed
        }
        return authentication != null ? authentication.getName() : null;
    }
    
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Jwt jwt = jwtToken.getToken();
            return jwt.getClaimAsString("preferred_username"); 
        }
        return authentication != null ? authentication.getName() : null;
    }

    public static List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
    
    public static boolean hasRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
        return getCurrentUserRoles().contains(roleWithPrefix);
    }
    
    public static boolean isManager() {
        return hasRole("MANAGER");
    }
    
    public static boolean isHr() {
        return hasRole("HR");
    }
    
    public static boolean isCeoOrAdmin() {
        return hasRole("CEO") || hasRole("ADMIN");
    }
}
