package br.com.fiap.carehub.historico;

import br.com.fiap.carehub.historico.enums.Role;
import br.com.fiap.carehub.historico.model.Usuario;
import br.com.fiap.carehub.historico.security.AcessoNegadoHandler;
import br.com.fiap.carehub.historico.security.UsuarioAutenticado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Consulta do historico via GraphQL (DEV TASK 07)")
public class HistoricoGraphQlTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void medicoVeOHistoricoDeQualquerPaciente() throws Exception {

        executar("{ historicoPorPaciente(pacienteId: \"2\") { id } }", medico())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.historicoPorPaciente.length()").value(2))
                .andExpect(jsonPath("$.data.historicoPorPaciente[0].id").value("4"))
                .andExpect(jsonPath("$.data.historicoPorPaciente[1].id").value("3"));
    }

    @Test
    void enfermeiroVeOHistoricoDeQualquerPaciente() throws Exception {

        executar("{ historicoPorPaciente(pacienteId: \"1\") { id } }", enfermeiro())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.historicoPorPaciente.length()").value(2))
                .andExpect(jsonPath("$.data.historicoPorPaciente[0].id").value("2"))
                .andExpect(jsonPath("$.data.historicoPorPaciente[1].id").value("1"));
    }

    @Test
    void pacienteVeSomenteOProprioHistorico() throws Exception {

        executar("{ historicoPorPaciente(pacienteId: \"1\") { id paciente { id nome } } }", paciente1())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.historicoPorPaciente.length()").value(2))
                .andExpect(jsonPath("$.data.historicoPorPaciente[0].paciente.id").value("1"))
                .andExpect(jsonPath("$.data.historicoPorPaciente[0].paciente.nome").value("Joao Silva"))
                .andExpect(jsonPath("$.data.historicoPorPaciente[1].paciente.id").value("1"));
    }

    @Test
    void pacienteNaoVeOHistoricoDeOutroPaciente() throws Exception {

        executar("{ historicoPorPaciente(pacienteId: \"2\") { id } }", paciente1())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.historicoPorPaciente").doesNotExist())
                .andExpect(jsonPath("$.errors[0].extensions.classification").value("FORBIDDEN"))
                .andExpect(jsonPath("$.errors[0].message").value(AcessoNegadoHandler.MENSAGEM));
    }

    @Test
    void pacienteNaoVeAsConsultasFuturasDeOutroPaciente() throws Exception {

        executar("{ consultasFuturasPorPaciente(pacienteId: \"2\") { id } }", paciente1())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.consultasFuturasPorPaciente").doesNotExist())
                .andExpect(jsonPath("$.errors[0].extensions.classification").value("FORBIDDEN"))
                .andExpect(jsonPath("$.errors[0].message").value(AcessoNegadoHandler.MENSAGEM));
    }

    @Test
    void consultasFuturasTrazemSomenteAsPosterioresAoInstanteAtual() throws Exception {

        executar("{ consultasFuturasPorPaciente(pacienteId: \"1\") { id dataHora } }", paciente1())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.consultasFuturasPorPaciente.length()").value(1))
                .andExpect(jsonPath("$.data.consultasFuturasPorPaciente[0].id").value("2"))
                .andExpect(jsonPath("$.data.consultasFuturasPorPaciente[0].dataHora")
                        .value("2030-09-15T10:00:00"));
    }

    @Test
    void filtroPorStatusRestringeOHistorico() throws Exception {

        executar("{ historicoPorPaciente(pacienteId: \"1\", status: REALIZADA) { id status } }", medico())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.historicoPorPaciente.length()").value(1))
                .andExpect(jsonPath("$.data.historicoPorPaciente[0].id").value("1"))
                .andExpect(jsonPath("$.data.historicoPorPaciente[0].status").value("REALIZADA"));
    }

    @Test
    void filtroPorStatusRestringeAsConsultasFuturas() throws Exception {

        executar("{ consultasFuturasPorPaciente(pacienteId: \"1\", status: CANCELADA) { id } }", medico())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.consultasFuturasPorPaciente.length()").value(0));
    }

    @Test
    void statusInvalidoERejeitadoPeloContrato() throws Exception {

        executar("{ historicoPorPaciente(pacienteId: \"1\", status: INEXISTENTE) { id } }", medico())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0].extensions.classification").value("ValidationError"));
    }

    @Test
    void campoInexistenteERejeitadoPeloContrato() throws Exception {

        executar("{ historicoPorPaciente(pacienteId: \"1\") { cpf } }", medico())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0].extensions.classification").value("ValidationError"));
    }

    @Test
    void oProfissionalVemJuntoDoAtendimento() throws Exception {

        executar("{ historicoPorPaciente(pacienteId: \"1\") { id profissional { id nome } } }", medico())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.historicoPorPaciente[0].profissional.id").value("1"))
                .andExpect(jsonPath("$.data.historicoPorPaciente[0].profissional.nome")
                        .value("Dr. Carlos Eduardo"));
    }

    @Test
    void requisicaoSemCredencialNaoAlcancaOEndpointGraphQl() throws Exception {

        String corpo = """
                {"query":"{ historicoPorPaciente(pacienteId: \\"1\\") { id } }"}
                """;

        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions executar(String query, UsuarioAutenticado usuario) throws Exception {

        String corpo = "{\"query\":%s}".formatted(quote(query));

        return mockMvc.perform(post("/graphql")
                .with(user(usuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }

    private String quote(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n") + "\"";
    }

    private UsuarioAutenticado medico() {
        return new UsuarioAutenticado(Usuario.builder()
                .username("medico1")
                .password("irrelevante")
                .role(Role.MEDICO)
                .ativo(true)
                .profissionalId(1L)
                .build());
    }

    private UsuarioAutenticado enfermeiro() {
        return new UsuarioAutenticado(Usuario.builder()
                .username("enfermeiro1")
                .password("irrelevante")
                .role(Role.ENFERMEIRO)
                .ativo(true)
                .profissionalId(2L)
                .build());
    }

    private UsuarioAutenticado paciente1() {
        return new UsuarioAutenticado(Usuario.builder()
                .username("paciente1")
                .password("irrelevante")
                .role(Role.PACIENTE)
                .ativo(true)
                .pacienteId(1L)
                .build());
    }
}
