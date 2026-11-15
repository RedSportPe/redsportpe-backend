package com.redsport.backend.shared.infrastructure.security;

import com.redsport.backend.identity.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import com.redsport.backend.identity.infrastructure.authorization.sfs.services.UserDetailsServiceImpl;
import com.redsport.backend.identity.infrastructure.tokens.jwt.services.TokenService;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public BearerAuthorizationRequestFilter bearerFilter(
      TokenService tokens, UserDetailsServiceImpl users) {
    return new BearerAuthorizationRequestFilter(tokens, users);
  }

  @Bean
  public FilterRegistrationBean<BearerAuthorizationRequestFilter> filterRegistration(
      BearerAuthorizationRequestFilter filter) {
    var registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  public SecurityFilterChain security(
      HttpSecurity http, BearerAuthorizationRequestFilter filter, CorsConfigurationSource cors)
      throws Exception {
    return http.cors(c -> c.configurationSource(cors))
        .csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, ex) -> {
                          res.setStatus(401);
                          res.setContentType("application/problem+json");
                          res.getWriter()
                              .write("{\"status\":401,\"detail\":\"Autenticación requerida\"}");
                        })
                    .accessDeniedHandler(
                        (req, res, ex) -> {
                          res.setStatus(403);
                          res.setContentType("application/problem+json");
                          res.getWriter().write("{\"status\":403,\"detail\":\"Acceso denegado\"}");
                        }))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(
                        "/api/auth/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/products/**",
                        "/api/promotions",
                        "/api/catalog-metadata/**",
                        "/api/media/**")
                    .permitAll()
                    .requestMatchers("/api/admin/**", "/api/stores/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/users")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/operator/**")
                    .hasRole("OPERATOR")
                    .requestMatchers("/api/orders/**", "/api/cart/**", "/api/favorites/**")
                    .hasRole("CUSTOMER")
                    .requestMatchers("/api/users/me/**")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public CorsConfigurationSource cors(
      @Value("${app.cors.allowed-origins:http://localhost:4200}") String origins) {
    var c = new CorsConfiguration();
    c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key"));
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return source;
  }
}
