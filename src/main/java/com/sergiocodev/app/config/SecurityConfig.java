package com.sergiocodev.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;

        @Value("${app.cors.allowed-origins:http://localhost:4200}")
        private String allowedOrigins;

        @Value("${app.swagger.secured:true}")
        private boolean swaggerSecured;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
                this.jwtAuthFilter = jwtAuthFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .authorizeHttpRequests(auth -> {
                                                // Public endpoints - NO authentication required
                                                auth.requestMatchers("/api/v1/auth/**").permitAll();
                                                auth.requestMatchers("/api/auth/**").permitAll();
                                                auth.requestMatchers("/uploads/**").permitAll();

                                                // Railway health check - must be public (no auth)
                                                auth.requestMatchers("/actuator/health").permitAll();

                                                // Swagger endpoints - configurable via app.swagger.secured
                                                // In production (true = default): requires ADMIN role
                                                // In development (false): open access for testing
                                                if (swaggerSecured) {
                                                    auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                                            "/v3/api-docs/**",
                                                            "/swagger-resources/**", "/webjars/**").hasRole("ADMIN");
                                                } else {
                                                    auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                                            "/v3/api-docs/**",
                                                            "/swagger-resources/**", "/webjars/**").permitAll();
                                                }

                                                // ALL other endpoints require JWT authentication
                                                auth.anyRequest().authenticated();
                                        })
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // JWT filter runs before standard authentication
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * Centralized CORS configuration.
         * Replaces per-controller @CrossOrigin annotations.
         * Configure allowed origins via app.cors.allowed-origins property.
         * Use comma-separated values for multiple origins.
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                List<String> origins = List.of(allowedOrigins.split(",")).stream()
                                .map(String::trim)
                                .toList();

                // Si el origen es "*" usamos setAllowedOriginPatterns para que sea
                // compatible con allowCredentials=true (el browser rechaza * + credentials).
                // Con allowedOriginPatterns Spring refleja el origen real del request.
                if (origins.size() == 1 && origins.get(0).equals("*")) {
                        configuration.setAllowedOriginPatterns(List.of("*"));
                } else {
                        configuration.setAllowedOrigins(origins);
                }
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setExposedHeaders(List.of("Content-Disposition"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        /**
         * Authentication manager
         */
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
                        throws Exception {
                return config.getAuthenticationManager();
        }

        /**
         * BCrypt password encoder
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
