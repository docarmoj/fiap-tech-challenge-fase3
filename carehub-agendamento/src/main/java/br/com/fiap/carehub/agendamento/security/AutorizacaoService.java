package br.com.fiap.carehub.agendamento.security;

import br.com.fiap.carehub.agendamento.enums.Role;
import br.com.fiap.carehub.agendamento.model.Consulta;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AutorizacaoService {

	private static final Set<Role> PERFIS_COM_ACESSO_TOTAL = Set.of(Role.MEDICO, Role.ENFERMEIRO);

	public void validarAcessoConsulta(Consulta consulta, UsuarioAutenticado usuario) {
		if (PERFIS_COM_ACESSO_TOTAL.contains(usuario.getRole())) {
			return;
		}

		if (usuario.getRole() != Role.PACIENTE) {
			throw new AccessDeniedException("Perfil sem acesso a consultas");
		}

		if (!consulta.getPaciente().getId().equals(pacienteVinculado(usuario))) {
			throw new AccessDeniedException("Consulta de outro paciente");
		}
	}

	public Optional<Long> filtroDeListagem(UsuarioAutenticado usuario) {
		if (PERFIS_COM_ACESSO_TOTAL.contains(usuario.getRole())) {
			return Optional.empty();
		}

		if (usuario.getRole() != Role.PACIENTE) {
			throw new AccessDeniedException("Perfil sem acesso a consultas");
		}

		return Optional.of(pacienteVinculado(usuario));
	}

	private Long pacienteVinculado(UsuarioAutenticado usuario) {
		Long pacienteId = usuario.getPacienteId();

		if (pacienteId == null) {
			throw new AccessDeniedException("Usuario com perfil de paciente sem vinculo");
		}

		return pacienteId;
	}
}
