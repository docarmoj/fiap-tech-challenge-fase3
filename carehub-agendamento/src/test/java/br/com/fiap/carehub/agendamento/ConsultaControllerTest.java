package br.com.fiap.carehub.agendamento;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void deveBuscarConsultaPorId() throws Exception {

        mockMvc.perform(get("/consultas/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deveCriarConsulta() throws Exception {

        String requestBody = """
                {
                  "pacienteId": 1,
                  "profissionalId": 1,
                  "dataHora": "2026-10-20T14:30:00",
                  "observacoes": "Consulta criada pelo teste"
                }
                """;

        mockMvc.perform(post("/consultas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.pacienteId").value(1))
                .andExpect(jsonPath("$.profissionalId").value(1))
                .andExpect(jsonPath("$.status").value("AGENDADA"));
    }

    @Test
    @WithMockUser
    void deveAtualizarConsulta() throws Exception {

        String requestBody = """
                {
                  "pacienteId": 1,
                  "profissionalId": 1,
                  "dataHora": "2026-11-10T09:00:00",
                  "status": "REALIZADA",
                  "observacoes": "Consulta atualizada pelo teste"
                }
                """;

        mockMvc.perform(put("/consultas/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pacienteId").value(1))
                .andExpect(jsonPath("$.profissionalId").value(1))
                .andExpect(jsonPath("$.status").value("REALIZADA"))
                .andExpect(jsonPath("$.observacoes").value("Consulta atualizada pelo teste"));
    }
}