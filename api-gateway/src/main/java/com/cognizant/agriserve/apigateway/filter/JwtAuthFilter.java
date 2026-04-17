package com.cognizant.agriserve.apigateway.filter;

import com.cognizant.agriserve.apigateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 1. Skip validation for Auth endpoints
        if (path.contains("/auth/")) {
            return chain.filter(exchange);
        }

        // 2. Check for Authorization Header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // 3. Validate Token (Authentication)
        if (!jwtUtil.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 4. Extract Info
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);

        // 5. Forward via headers to downstream microservices
        // The downstream services will now use @PreAuthorize to check the X-User-Role
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder
                        .header("X-Logged-In-User-Id", String.valueOf(userId))
                        .header("X-User-Name", username)
                        .header("X-User-Role", role)
                        .build())
                .build();

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}