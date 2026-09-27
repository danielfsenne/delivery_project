package com.rota.auth.infrastructure.config;

import com.rota.common.security.JwtService;
import com.rota.common.security.StatelessSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Clock;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http.with(StatelessSecurity.jwt(jwtService), c -> {});
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/refresh", "/auth/logout")
                .permitAll()
                .requestMatchers("/actuator/health/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                .permitAll()
                .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Evita o usuário padrão gerado pelo Spring Boot; a autenticação é feita pelo AuthService. */
    @Bean
    UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
