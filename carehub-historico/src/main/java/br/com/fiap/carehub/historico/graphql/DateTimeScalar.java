package br.com.fiap.carehub.historico.graphql;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class DateTimeScalar {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static final GraphQLScalarType INSTANCE = GraphQLScalarType.newScalar()
            .name("DateTime")
            .description("Data e hora local no formato ISO-8601, sem fuso. Exemplo: 2030-09-15T10:00:00")
            .coercing(new Coercing<LocalDateTime, String>() {

                @Override
                public String serialize(Object valor, GraphQLContext contexto, Locale locale) {

                    if (valor instanceof LocalDateTime dataHora) {
                        return FORMATO.format(dataHora);
                    }

                    throw new CoercingSerializeException(
                            "Esperado LocalDateTime, recebido " + tipoDe(valor));
                }

                @Override
                public LocalDateTime parseValue(Object valor, GraphQLContext contexto, Locale locale) {

                    if (valor instanceof String texto) {
                        return converter(texto, CoercingParseValueException::new);
                    }

                    throw new CoercingParseValueException(
                            "Esperado texto ISO-8601, recebido " + tipoDe(valor));
                }

                @Override
                public LocalDateTime parseLiteral(Value<?> valor, CoercedVariables variaveis,
                        GraphQLContext contexto, Locale locale) {

                    if (valor instanceof StringValue texto) {
                        return converter(texto.getValue(), CoercingParseLiteralException::new);
                    }

                    throw new CoercingParseLiteralException(
                            "Esperado texto ISO-8601, recebido " + tipoDe(valor));
                }

                @Override
                public Value<?> valueToLiteral(Object valor, GraphQLContext contexto, Locale locale) {
                    return StringValue.newStringValue(serialize(valor, contexto, locale)).build();
                }
            })
            .build();

    private DateTimeScalar() {
    }

    private static LocalDateTime converter(String texto,
            java.util.function.Function<String, RuntimeException> erro) {
        try {
            return LocalDateTime.parse(texto, FORMATO);
        } catch (DateTimeParseException e) {
            throw erro.apply("Data e hora invalida: " + texto);
        }
    }

    private static String tipoDe(Object valor) {
        return valor == null ? "null" : valor.getClass().getSimpleName();
    }
}
