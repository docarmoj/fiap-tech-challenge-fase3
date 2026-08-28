package br.com.fiap.carehub.historico.config;

import br.com.fiap.carehub.historico.security.AcessoNegadoHandler;
import br.com.fiap.carehub.historico.security.AutenticacaoEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Mesmas credenciais e mesmo modelo de perfil do carehub-agendamento, sobre a tabela
// de usuarios deste banco. O endpoint GraphQL exige autenticacao como qualquer outra rota.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final AutenticacaoEntryPoint autenticacaoEntryPoint;
    private final AcessoNegadoHandler acessoNegadoHandler;

    public SecurityConfig(AutenticacaoEntryPoint autenticacaoEntryPoint,
            AcessoNegadoHandler acessoNegadoHandler) {
        this.autenticacaoEntryPoint = autenticacaoEntryPoint;
        this.acessoNegadoHandler = acessoNegadoHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint(autenticacaoEntryPoint))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(autenticacaoEntryPoint)
                        .accessDeniedHandler(acessoNegadoHandler)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
