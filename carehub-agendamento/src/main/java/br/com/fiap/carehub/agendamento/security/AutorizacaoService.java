package br.com.fiap.carehub.agendamento.security;

import br.com.fiap.carehub.agendamento.enums.Role;
import br.com.fiap.carehub.agendamento.model.Consulta;
import br.com.fiap.carehub.agendamento.repository.ConsultaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Serviço de autorização para validar acesso a recursos específicos baseado em roles e dados do usuário.
 */
@Service
public class AutorizacaoService {

	private final ConsultaRepository consultaRepository;

	public AutorizacaoService(ConsultaRepository consultaRepository) {
		this.consultaRepository = consultaRepository;
	}

	/**
	 * Valida se o usuário autenticado pode acessar a consulta fornecida.
	 * 
	 * Regras:
	 * - MEDICO: pode acessar qualquer consulta
	 * - ENFERMEIRO: pode acessar qualquer consulta
	 * - PACIENTE: pode acessar apenas suas próprias consultas
	 */
	public void validarAcessoConsulta(Long consultaId, UsuarioAutenticado usuario) {
		if (usuario.getRole() == Role.MEDICO || usuario.getRole() == Role.ENFERMEIRO) {
			return;
		}

		Consulta consulta = consultaRepository.findById(consultaId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Consulta não encontrada"
				));

		if (usuario.getRole() == Role.PACIENTE) {
			if (!consulta.getPaciente().getId().equals(usuario.getPacienteId())) {
				throw new ResponseStatusException(
						HttpStatus.FORBIDDEN,
						"Acesso negado: você não pode visualizar consultas de outros pacientes"
				);
			}
		}
	}

	/**
	 * Valida se o usuário autenticado pode criar uma consulta para o paciente fornecido.
	 * 
	 * Regras:
	 * - MEDICO: pode criar consultas para qualquer paciente
	 * - ENFERMEIRO: pode criar consultas para qualquer paciente
	 * - PACIENTE: pode criar consultas apenas para si mesmo
	 */
	public void validarCriacaoConsulta(Long pacienteId, UsuarioAutenticado usuario) {
		if (usuario.getRole() == Role.MEDICO || usuario.getRole() == Role.ENFERMEIRO) {
			return;
		}

		if (usuario.getRole() == Role.PACIENTE) {
			if (!pacienteId.equals(usuario.getPacienteId())) {
				throw new ResponseStatusException(
						HttpStatus.FORBIDDEN,
						"Acesso negado: você não pode criar consultas para outros pacientes"
				);
			}
		}
	}

	/**
	 * Valida se o usuário autenticado pode atualizar a consulta fornecida.
	 * 
	 * Regras:
	 * - MEDICO: pode atualizar qualquer consulta
	 * - ENFERMEIRO: pode atualizar qualquer consulta
	 * - PACIENTE: não pode atualizar consultas (read-only)
	 */
	public void validarAtualizacaoConsulta(Long consultaId, UsuarioAutenticado usuario) {
		if (usuario.getRole() == Role.MEDICO || usuario.getRole() == Role.ENFERMEIRO) {
			return;
		}

		if (usuario.getRole() == Role.PACIENTE) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN,
					"Acesso negado: pacientes não podem atualizar consultas"
			);
		}
	}
}
