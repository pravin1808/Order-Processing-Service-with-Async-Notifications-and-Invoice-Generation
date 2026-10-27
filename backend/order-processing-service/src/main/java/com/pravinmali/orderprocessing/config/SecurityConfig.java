package com.pravinmali.orderprocessing.config;

import com.pravinmali.orderprocessing.security.JwtAuthenticationFilter;
import com.pravinmali.orderprocessing.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
 * Spring Security configuration — JWT-stateless, RBAC rules.
 *
 * <h3>Role-based access rules:</h3>
 * <table>
 *   <tr><td>POST /auth/**</td><td>Public</td></tr>
 *   <tr><td>GET /orders</td><td>ADMIN only — sees all orders</td></tr>
 *   <tr><td>POST /orders</td><td>ADMIN or CUSTOMER</td></tr>
 *   <tr><td>GET /orders/{id}</td><td>ADMIN or CUSTOMER (service enforces ownership)</td></tr>
 *   <tr><td>PATCH /orders/{id}/status</td><td>ADMIN only</td></tr>
 *   <tr><td>GET /orders/{id}/invoice</td><td>ADMIN or CUSTOMER (service enforces ownership)</td></tr>
 *   <tr><td>GET /export/**</td><td>ADMIN only</td></tr>
 * </table>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)          // REST API — no CSRF needed
            .sessionManagement(sm -> sm
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // No sessions
            .authorizeHttpRequests(auth -> auth
                    // Public — no token needed
                    .requestMatchers(PUBLIC_PATHS).permitAll()

                    // Order management
                    .requestMatchers(HttpMethod.POST,  "/api/v1/orders").hasAnyRole("ADMIN", "CUSTOMER")
                    .requestMatchers(HttpMethod.GET,   "/api/v1/orders").hasAnyRole("ADMIN", "CUSTOMER")
                    .requestMatchers(HttpMethod.GET,   "/api/v1/orders/**").hasAnyRole("ADMIN", "CUSTOMER")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/orders/**").hasRole("ADMIN")

                    // Export — admin only
                    .requestMatchers("/api/v1/export/**").hasRole("ADMIN")

                    // Everything else requires authentication
                    .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
