package br.com.fiap.carehub.historico;

import br.com.fiap.carehub.historico.enums.Role;
import br.com.fiap.carehub.historico.security.UsuarioAutenticado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// O servico de historico tem credenciais proprias, na tabela do proprio banco.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Autenticacao do servico de historico")
public class AutenticacaoHistoricoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    void deveCarregarPacienteComVinculoDePaciente() {

        UsuarioAutenticado usuario =
                (UsuarioAutenticado) userDetailsService.loadUserByUsername("paciente1");

        assertThat(usuario.getRole()).isEqualTo(Role.PACIENTE);
        assertThat(usuario.getPacienteId()).isEqualTo(1L);
        assertThat(usuario.getProfissionalId()).isNull();
        assertThat(usuario.isEnabled()).isTrue();
    }

    @Test
    void deveCarregarMedicoComVinculoDeProfissional() {

        UsuarioAutenticado usuario =
                (UsuarioAutenticado) userDetailsService.loadUserByUsername("medico1");

        assertThat(usuario.getRole()).isEqualTo(Role.MEDICO);
        assertThat(usuario.getProfissionalId()).isEqualTo(1L);
        assertThat(usuario.getPacienteId()).isNull();
    }

    @Test
    void usuarioInativoNaoFicaHabilitado() {

        UsuarioAutenticado usuario =
                (UsuarioAutenticado) userDetailsService.loadUserByUsername("inativo1");

        assertThat(usuario.isEnabled()).isFalse();
    }

    @Test
    void usuarioInexistenteNaoEncontrado() {

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ninguem"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void requisicaoSemCredencialRecebe401ComDesafioBasic() throws Exception {

        mockMvc.perform(get("/graphql"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"carehub\""));
    }
}
