package br.com.fiap.carehub.agendamento.security;

import br.com.fiap.carehub.agendamento.enums.Role;
import br.com.fiap.carehub.agendamento.messaging.ConsultaEventPublisher;
import br.com.fiap.carehub.agendamento.model.Paciente;
import br.com.fiap.carehub.agendamento.model.Profissional;
import br.com.fiap.carehub.agendamento.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AutorizacaoConsultaTest {

    private static final long CONSULTA_DO_PACIENTE_2 = 3L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaEventPublisher consultaEventPublisher;

    private UsuarioAutenticado medico() {
        return profissional(Role.MEDICO, 1L, "medico.teste");
    }

    private UsuarioAutenticado enfermeiro() {
        return profissional(Role.ENFERMEIRO, 2L, "enfermeiro.teste");
    }

    private UsuarioAutenticado profissional(Role role, Long profissionalId, String username) {
        Usuario usuario = Usuario.builder()
                .username(username)
                .password("123456")
                .role(role)
                .ativo(true)
                .profissional(Profissional.builder().id(profissionalId).build())
                .build();

        return new UsuarioAutenticado(usuario);
    }

    private UsuarioAutenticado paciente(Long pacienteId) {
        Usuario usuario = Usuario.builder()
                .username("paciente" + pacienteId)
                .password("123456")
                .role(Role.PACIENTE)
                .ativo(true)
                .paciente(Paciente.builder().id(pacienteId).build())
                .build();

        return new UsuarioAutenticado(usuario);
    }

    private String consultaValida(long pacienteId) {
        return """
                {
                  "pacienteId": %d,
                  "profissionalId": 1,
                  "dataHora": "2030-12-01T09:00:00",
                  "observacoes": "Consulta do teste de autorizacao"
                }
                """.formatted(pacienteId);
    }

    private String atualizacaoValida() {
        return """
                {
                  "pacienteId": 1,
                  "profissionalId": 1,
                  "dataHora": "2030-12-02T09:00:00",
                  "status": "AGENDADA",
                  "observacoes": "Consulta atualizada pelo teste"
                }
                """;
    }

    @Test
    void medicoListaConsultasDeTodosOsPacientes() throws Exception {
        mockMvc.perform(get("/consultas").with(user(medico())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$[?(@.pacienteId == 1)]", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.pacienteId == 2)]", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void medicoBuscaConsultaDeQualquerPaciente() throws Exception {
        mockMvc.perform(get("/consultas/" + CONSULTA_DO_PACIENTE_2).with(user(medico())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(2));
    }

    @Test
    void medicoCriaConsulta() throws Exception {
        mockMvc.perform(post("/consultas")
                        .with(user(medico()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaValida(1)))
                .andExpect(status().isCreated());
    }

    @Test
    void medicoAtualizaConsulta() throws Exception {
        mockMvc.perform(put("/consultas/1")
                        .with(user(medico()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacaoValida()))
                .andExpect(status().isOk());
    }

    @Test
    void enfermeiroListaConsultasDeTodosOsPacientes() throws Exception {
        mockMvc.perform(get("/consultas").with(user(enfermeiro())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.pacienteId == 2)]", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void enfermeiroCriaConsulta() throws Exception {
        mockMvc.perform(post("/consultas")
                        .with(user(enfermeiro()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaValida(2)))
                .andExpect(status().isCreated());
    }

    @Test
    void enfermeiroAtualizaConsulta() throws Exception {
        mockMvc.perform(put("/consultas/1")
                        .with(user(enfermeiro()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacaoValida()))
                .andExpect(status().isOk());
    }

    @Test
    void pacienteListaSomenteAsProprias() throws Exception {
        mockMvc.perform(get("/consultas").with(user(paciente(2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].pacienteId", everyItem(is(2))));
    }

    @Test
    void pacienteBuscaConsultaPropria() throws Exception {
        mockMvc.perform(get("/consultas/" + CONSULTA_DO_PACIENTE_2).with(user(paciente(2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(2));
    }

    @Test
    void pacienteNaoBuscaConsultaDeOutroPaciente() throws Exception {
        mockMvc.perform(get("/consultas/" + CONSULTA_DO_PACIENTE_2).with(user(paciente(1L))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void pacienteNaoCriaConsulta() throws Exception {
        mockMvc.perform(post("/consultas")
                        .with(user(paciente(1L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaValida(1)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void pacienteNaoAtualizaConsulta() throws Exception {
        mockMvc.perform(put("/consultas/1")
                        .with(user(paciente(1L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacaoValida()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void negativaPorPerfilENegativaPorVinculoUsamOMesmoCorpo() throws Exception {
        String porPerfil = mockMvc.perform(post("/consultas")
                        .with(user(paciente(1L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaValida(1)))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentType();

        String porVinculo = mockMvc.perform(get("/consultas/" + CONSULTA_DO_PACIENTE_2)
                        .with(user(paciente(1L))))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentType();

        org.assertj.core.api.Assertions.assertThat(porVinculo).isEqualTo(porPerfil);
    }

    @Test
    void pacienteSemVinculoNaoLista() throws Exception {
        Usuario semVinculo = Usuario.builder()
                .username("paciente.sem.vinculo")
                .password("123456")
                .role(Role.PACIENTE)
                .ativo(true)
                .build();

        mockMvc.perform(get("/consultas").with(user(new UsuarioAutenticado(semVinculo))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void consultaInexistenteResponde404ParaProfissional() throws Exception {
        mockMvc.perform(get("/consultas/9999").with(user(medico())))
                .andExpect(status().isNotFound());
    }
}
