package com.medhelp.medhelp;

import com.medhelp.common.security.JwtAuthFilter;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
 
/**
 * Spring Security configuration.
 *
 * KEY DECISIONS:
 *  - STATELESS sessions: no server-side session storage; JWT is the only auth
 *  - CSRF disabled:      safe for REST APIs (CSRF is for browser form-based auth)
 *  - PUBLIC_URLS:        these endpoints work without a token (login, patient portal, Swagger)
 *  - @EnableMethodSecurity: enables @PreAuthorize on controller/service methods
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class Securityconfig {
    
 
    private final JwtAuthFilter jwtauthfilter;
    /** Endpoints that don't require a JWT */
    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/patient/otp/send",
            "/api/v1/auth/patient/otp/verify",
            "/api/v1/reports/public/**",         // shareable report links — no login needed
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/actuator/health"
    };
 
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .anyRequest().authenticated()
                )
                // Add our JWT filter BEFORE Spring's default username/password filter
                .addFilterBefore( (Filter) jwtauthfilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
 
    /**
     * BCrypt password encoder.
     * BCrypt is deliberately slow (strength 10 = ~100ms per hash) to slow down brute-force attacks.
     * Used in AuthService to verify login passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
