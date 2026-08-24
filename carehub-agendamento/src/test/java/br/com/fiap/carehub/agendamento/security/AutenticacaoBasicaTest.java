package br.com.fiap.carehub.agendamento.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Autenticacao basica (DEV TASK 04)")
class AutenticacaoBasicaTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("requisicao sem credenciais e recusada com 401 e desafio Basic")
	void requisicaoSemCredenciaisRetorna401() throws Exception {
		mockMvc.perform(get("/usuarios/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", org.hamcrest.Matchers.containsString("Basic")));
	}

	@Test
	@DisplayName("credenciais validas do banco autenticam e o usuario e identificado")
	void credenciaisValidasIdentificamOUsuario() throws Exception {
		mockMvc.perform(get("/usuarios/me").with(httpBasic("medico1", "123456")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("medico1"))
				.andExpect(jsonPath("$.role").value("MEDICO"))
				.andExpect(jsonPath("$.profissionalId").value(1))
				.andExpect(jsonPath("$.pacienteId").doesNotExist());
	}

	@Test
	@DisplayName("senha incorreta nao autentica")
	void senhaIncorretaRetorna401() throws Exception {
		mockMvc.perform(get("/usuarios/me").with(httpBasic("medico1", "senha-errada")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("usuario desativado nao autentica mesmo com a senha correta")
	void usuarioInativoRetorna401() throws Exception {
		mockMvc.perform(get("/usuarios/me").with(httpBasic("inativo1", "123456")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("paciente e identificado pelo seu pacienteId, sem vinculo profissional")
	void pacienteEIdentificadoPeloPacienteId() throws Exception {
		mockMvc.perform(get("/usuarios/me").with(httpBasic("paciente1", "123456")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("PACIENTE"))
				.andExpect(jsonPath("$.pacienteId").value(1))
				.andExpect(jsonPath("$.profissionalId").doesNotExist());
	}

	@Test
	@DisplayName("erro de autenticacao responde em formato ProblemDetail")
	void erroDeAutenticacaoUsaProblemDetail() throws Exception {
		mockMvc.perform(get("/usuarios/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.title").value("Nao autenticado"))
				.andExpect(jsonPath("$.detail").exists());
	}
}
