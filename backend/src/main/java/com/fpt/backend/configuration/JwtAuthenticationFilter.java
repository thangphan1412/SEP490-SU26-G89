package com.fpt.backend.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserDetailService userDetailsService;
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }
        String header = request.getHeader("Authorization");
        String jwtToken = null;
        String email;
        if(header == null || !header.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        jwtToken = header.substring(7);
        try {
            email = jwtService.extractUsername(jwtToken);
            if (email == null || email.isBlank()) {
                unauthorized(response, false);
                return;
            }
            if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
                MyUserDetail userDetails = (MyUserDetail) userDetailsService.loadUserByUsername(email);
                if (jwtService.isTokenValid(jwtToken, userDetails)){
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    unauthorized(response, false);
                    return;
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException expired) {
            unauthorized(response, true);
            return;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException
                 | org.springframework.security.core.userdetails.UsernameNotFoundException invalid) {
            unauthorized(response, false);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, boolean expired) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(expired
                ? "{\"status\":401,\"code\":\"TOKEN_EXPIRED\",\"message\":\"Your session has expired. Please sign in again.\"}"
                : "{\"status\":401,\"code\":\"INVALID_TOKEN\",\"message\":\"Your session is invalid. Please sign in again.\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        System.out.println(request.getServletPath());
        return path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/forgot-password")
                || path.equals("/api/v1/auth/reset-password");
    }
}
