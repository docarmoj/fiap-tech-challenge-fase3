package br.com.fiap.carehub.historico.config;

import br.com.fiap.carehub.historico.graphql.DateTimeScalar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQlConfig {

    @Bean
    public RuntimeWiringConfigurer dateTimeScalarConfigurer() {
        return wiring -> wiring.scalar(DateTimeScalar.INSTANCE);
    }
}
