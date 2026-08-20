package br.com.fiap.carehub.historico.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_consulta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaHistorico {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "paciente_id", nullable = false)
	private Long pacienteId;

	@Column(name = "data_hora", nullable = false)
	private LocalDateTime dataHora;

	private String status;
	private String observacoes;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "profissional_id")
	private ProfissionalHistorico profissional;
}