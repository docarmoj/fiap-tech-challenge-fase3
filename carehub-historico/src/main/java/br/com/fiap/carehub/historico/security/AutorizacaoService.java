package br.com.fiap.carehub.historico.security;

import br.com.fiap.carehub.historico.enums.Role;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AutorizacaoService {

    private static final Set<Role> PERFIS_COM_ACESSO_TOTAL = Set.of(Role.MEDICO, Role.ENFERMEIRO);

    public void validarAcessoHistorico(Long pacienteId, UsuarioAutenticado usuario) {

        if (PERFIS_COM_ACESSO_TOTAL.contains(usuario.getRole())) {
            return;
        }

        if (usuario.getRole() != Role.PACIENTE) {
            throw new AccessDeniedException("Perfil sem acesso ao historico");
        }

        if (!pacienteId.equals(pacienteVinculado(usuario))) {
            throw new AccessDeniedException("Historico de outro paciente");
        }
    }

    private Long pacienteVinculado(UsuarioAutenticado usuario) {

        Long pacienteId = usuario.getPacienteId();

        if (pacienteId == null) {
            throw new AccessDeniedException("Usuario com perfil de paciente sem vinculo");
        }

        return pacienteId;
    }
}
