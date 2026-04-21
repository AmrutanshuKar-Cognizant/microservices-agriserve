package com.cognizant.agriserve.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                // 1. Disable CSRF (Not needed for stateless JWT APIs)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 2. CRITICAL: Tell Spring Security NOT to create sessions.
                // This saves memory and prevents WebFlux redirect bugs.
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // 3. Let all requests pass through Spring Security.
                // Your custom JwtAuthFilter will catch and validate them!
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**", "/api/auth/**").permitAll()
                        .anyExchange().permitAll()
                )

                // 4. Disable default browser pop-up login screens
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .build();
    }
}