package br.com.fiap.carehub.agendamento.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

// Resposta 403 para usuario autenticado sem permissao (usado pela DEV TASK 05)
@Component
public class AcessoNegadoHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       @NonNull AccessDeniedException accessDeniedException) throws IOException {

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Seu perfil nao permite executar esta operacao.");
        problema.setTitle("Acesso negado");
        problema.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {
                  "type":"about:blank",
                  "title":"Acesso negado",
                  "status":403,
                  "detail":"Seu perfil nao permite executar esta operacao.",
                  "instance":"%s"
                }
                """.formatted(request.getRequestURI()).replace("\r", "").replace("\n", ""));
    }
}
