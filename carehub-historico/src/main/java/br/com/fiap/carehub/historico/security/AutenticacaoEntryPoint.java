package br.com.fiap.carehub.historico.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

// Resposta 401: mantem o desafio WWW-Authenticate e devolve corpo em ProblemDetail
@Component
public class AutenticacaoEntryPoint implements AuthenticationEntryPoint {

    private static final String REALM = "carehub";

    private final ObjectMapper objectMapper;

    public AutenticacaoEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Credenciais ausentes ou invalidas. Use autenticacao basica (usuario e senha).");
        problema.setTitle("Nao autenticado");
        problema.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problema);
    }
}
