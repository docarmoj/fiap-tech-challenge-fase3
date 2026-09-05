package br.com.fiap.carehub.historico.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

// Resposta 401 para requisicao sem credencial valida
@Component
public class AutenticacaoEntryPoint implements AuthenticationEntryPoint {

    private static final String REALM = "carehub";

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException {

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Credenciais ausentes ou invalidas. Use autenticacao basica (usuario e senha).");
        problema.setTitle("Nao autenticado");
        problema.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {
                  "type":"about:blank",
                  "title":"Nao autenticado",
                  "status":401,
                  "detail":"Credenciais ausentes ou invalidas. Use autenticacao basica (usuario e senha).",
                  "instance":"%s"
                }
                """.formatted(request.getRequestURI()).replace("\r", "").replace("\n", ""));
    }
}
