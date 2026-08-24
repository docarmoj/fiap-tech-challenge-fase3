package br.com.fiap.carehub.agendamento.model;

import br.com.fiap.carehub.agendamento.enums.Role;
import jakarta.persistence.*;
import lombok.*;

// Credencial de acesso. Aponta para um Paciente OU um Profissional,
// e esse vinculo que responde "quais consultas sao minhas" na autorizacao.
@Entity
@Table(name = "tb_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profissional_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional profissional;

    public Long getPacienteId() {
        return paciente == null ? null : paciente.getId();
    }

    public Long getProfissionalId() {
        return profissional == null ? null : profissional.getId();
    }
}
