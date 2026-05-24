package com.medhelp.common.security;
import com.medhelp.common.tenant.TenantContext;

import org.springframework.lang.NonNull;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
 
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
 
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        try {
            String token = extractBearerToken(request);
 
            if (token != null && jwtTokenProvider.isValid(token)) {
                Claims claims = jwtTokenProvider.parseClaims(token);
 
                UUID userId = UUID.fromString(claims.getSubject());
                UUID labId  = UUID.fromString(claims.get("labId", String.class));
                String role = claims.get("role", String.class);
 
                // 1. Set tenant context — now any service can call TenantContext.get()
                TenantContext.set(labId);
 
                // 2. Set Spring Security context — enables @PreAuthorize("hasRole('OWNER')")
                var auth = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            log.error("Could not set authentication context: {}", e.getMessage());
        }
 
        try {
            chain.doFilter(request, response);   // call the next filter / controller
        } finally {
            // ALWAYS clear — ThreadLocal + SecurityContext must be cleaned up
            // or the next request reusing this thread will see stale data
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
 
    /** Extracts the raw token from "Authorization: Bearer eyJ..." */
    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
