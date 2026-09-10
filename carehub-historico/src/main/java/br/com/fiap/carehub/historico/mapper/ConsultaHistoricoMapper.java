package br.com.fiap.carehub.historico.mapper;

import br.com.fiap.carehub.historico.dto.ConsultaResponse;
import br.com.fiap.carehub.historico.model.ConsultaHistorico;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConsultaHistoricoMapper {

    @Mapping(target = "id", source = "consultaId")
    @Mapping(target = "paciente.id", source = "pacienteId")
    @Mapping(target = "paciente.nome", source = "pacienteNome")
    @Mapping(target = "profissional.id", source = "profissionalId")
    @Mapping(target = "profissional.nome", source = "profissionalNome")
    ConsultaResponse toResponse(ConsultaHistorico consulta);

    List<ConsultaResponse> toResponseList(List<ConsultaHistorico> consultas);
}
