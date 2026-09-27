package com.rota.common.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração base compartilhada: API stateless, sem CSRF, autenticação via JWT
 * e 401 para requisições sem token. Cada serviço aplica isto e define suas próprias regras.
 *
 * <pre>
 * http.with(StatelessSecurity.jwt(jwtService), c -> {});
 * http.authorizeHttpRequests(...);
 * </pre>
 */
public class StatelessSecurity extends AbstractHttpConfigurer<StatelessSecurity, HttpSecurity> {

    private final JwtService jwtService;

    private StatelessSecurity(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public static StatelessSecurity jwt(JwtService jwtService) {
        return new StatelessSecurity(jwtService);
    }

    @Override
    public void init(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
    }

    @Override
    public void configure(HttpSecurity http) {
        http.addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
    }
}
