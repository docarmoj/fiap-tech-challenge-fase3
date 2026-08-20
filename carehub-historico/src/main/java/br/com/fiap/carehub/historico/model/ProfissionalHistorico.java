package br.com.fiap.carehub.historico.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_profissional")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalHistorico {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nome;
	private String especialidade;
}