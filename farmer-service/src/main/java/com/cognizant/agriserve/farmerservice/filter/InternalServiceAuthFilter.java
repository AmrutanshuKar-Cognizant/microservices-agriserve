package com.cognizant.agriserve.farmerservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class InternalServiceAuthFilter extends OncePerRequestFilter {

    @Value("${internal.service.key}")
    private String internalServiceKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestInternalKey = request.getHeader("X-Internal-service-key");
        String gatewayRole = request.getHeader("X-User-Role");
        String username = request.getHeader("X-User-Name");

        if (internalServiceKey.equals(requestInternalKey)) {
            var auth = new UsernamePasswordAuthenticationToken("internal", null, List.of(new SimpleGrantedAuthority("ROLE_SERVICE")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        else if (gatewayRole != null && !gatewayRole.isEmpty()) {

            // --- CRITICAL FIX: Clean up the Double Header Bug here ---
            // If Feign sends "SERVICE, SERVICE", this safely reduces it to "SERVICE"
            gatewayRole = gatewayRole.split(",")[0].trim();

            String finalRole = gatewayRole.startsWith("ROLE_") ? gatewayRole : "ROLE_" + gatewayRole;
            var auth = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority(finalRole)));

            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        else {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}