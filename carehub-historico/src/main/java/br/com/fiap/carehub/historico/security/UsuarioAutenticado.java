package br.com.fiap.carehub.historico.security;

import br.com.fiap.carehub.historico.enums.Role;
import br.com.fiap.carehub.historico.model.Usuario;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// Usuario logado. Guarda tambem o vinculo com paciente ou profissional,
// usado nas regras de acesso.
public class UsuarioAutenticado implements UserDetails {

    private final String username;
    private final String password;
    private final Role role;
    private final boolean ativo;
    private final Long pacienteId;
    private final Long profissionalId;

    public UsuarioAutenticado(Usuario usuario) {
        this.username = usuario.getUsername();
        this.password = usuario.getPassword();
        this.role = usuario.getRole();
        this.ativo = usuario.isAtivo();
        this.pacienteId = usuario.getPacienteId();
        this.profissionalId = usuario.getProfissionalId();
    }

    // No banco a coluna guarda so MEDICO, ENFERMEIRO ou PACIENTE
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public Role getRole() {
        return role;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public Long getProfissionalId() {
        return profissionalId;
    }
}
