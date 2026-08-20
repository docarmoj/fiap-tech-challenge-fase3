// carehub-agendamento/src/main/java/br/com/fiap/carehub/agendamento/model/Profissional.java
package br.com.fiap.carehub.agendamento.model;

import br.com.fiap.carehub.agendamento.enums.TipoProfissional;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profissionais")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 20)
    private String registroProfissional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoProfissional tipo;

    @Column(length = 100)
    private String especialidade;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "profissional", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Consulta> consultas = new ArrayList<>();
}
