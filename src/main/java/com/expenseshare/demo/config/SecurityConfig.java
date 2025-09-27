package com.expenseshare.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/h2-console/**").permitAll() // Allow H2 Console access
//                        .anyRequest().authenticated()
//                )
//                .csrf(csrf -> csrf.disable()) // Disable CSRF for H2 Console (or configure specific exclusions)
//                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())); // Allow H2 Console to render in frames
//
return http.build();
    }
}