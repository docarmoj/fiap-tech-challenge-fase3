package br.com.fiap.carehub.historico.graphql;

import br.com.fiap.carehub.historico.security.AcessoNegadoHandler;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AcessoNegadoExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable excecao, DataFetchingEnvironment ambiente) {

        if (!(excecao instanceof AccessDeniedException)) {
            return null;
        }

        return GraphqlErrorBuilder.newError(ambiente)
                .errorType(ErrorType.FORBIDDEN)
                .message(AcessoNegadoHandler.MENSAGEM)
                .build();
    }
}
