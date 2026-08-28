package br.com.fiap.carehub.historico.model;

import br.com.fiap.carehub.historico.enums.StatusConsulta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Read model alimentado pelo evento de consulta do carehub-agendamento.
// Desnormalizado de proposito: nome de paciente e de profissional ficam na propria linha,
// entao a leitura e um SELECT unico e o historico nao replica os cadastros.
@Entity
@Table(name = "tb_consulta_historico")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consulta_id", nullable = false, unique = true)
    private Long consultaId;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "paciente_nome", nullable = false, length = 150)
    private String pacienteNome;

    @Column(name = "profissional_id", nullable = false)
    private Long profissionalId;

    @Column(name = "profissional_nome", nullable = false, length = 150)
    private String profissionalNome;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConsulta status;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
}
