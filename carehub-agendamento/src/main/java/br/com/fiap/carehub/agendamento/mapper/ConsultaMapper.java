package br.com.fiap.carehub.agendamento.mapper;

import br.com.fiap.carehub.agendamento.dto.ConsultaResponse;
import br.com.fiap.carehub.agendamento.model.Consulta;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConsultaMapper {

    @Mapping(target = "pacienteId", source = "paciente.id")
    @Mapping(target = "pacienteNome", source = "paciente.nome")
    @Mapping(target = "profissionalId", source = "profissional.id")
    @Mapping(target = "profissionalNome", source = "profissional.nome")
    ConsultaResponse toResponse(Consulta consulta);

    List<ConsultaResponse> toResponseList(List<Consulta> consultas);
}
