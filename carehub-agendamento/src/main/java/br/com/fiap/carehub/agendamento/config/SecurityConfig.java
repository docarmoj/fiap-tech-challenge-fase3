package br.com.fiap.carehub.agendamento.config;

import br.com.fiap.carehub.agendamento.security.AcessoNegadoHandler;
import br.com.fiap.carehub.agendamento.security.AutenticacaoEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Autenticacao basica (DEV TASK 04). Toda a API exige login;
// as regras por perfil vem na DEV TASK 05, via @PreAuthorize nos services.
@Configuration
@EnableWebSecurity
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
				// API stateless consumida por Postman/servicos: sem sessao, sem CSRF.
				// Se algum dia for consumida por navegador, CSRF tem que voltar.
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.headers(headers -> headers
						.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/h2-console/**").permitAll()
						.anyRequest().authenticated()
				)
				.httpBasic(basic -> basic.authenticationEntryPoint(autenticacaoEntryPoint))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(autenticacaoEntryPoint)
						.accessDeniedHandler(acessoNegadoHandler)
				);

		return http.build();
	}

	// Delegante: le o prefixo {bcrypt} gravado junto da senha
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}
