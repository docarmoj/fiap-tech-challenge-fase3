# CareHub API — Tech Challenge Fase 3

Backend modular para agendamento de consultas, histórico do paciente e notificações assíncronas em ambiente hospitalar, com autenticação HTTP Basic, controle de acesso por perfil e comunicação desacoplada entre serviços.

## Acesso

Link do repositório:
https://github.com/docarmoj/fiap-tech-challenge-fase3

## Arquitetura

O repositório está organizado como monorepo com 3 serviços independentes:

| Serviço | Porta | Responsabilidade |
| --- | --- | --- |
| `carehub-agendamento` | `8081` | autenticação, autorização e API REST para listar, consultar, criar e atualizar consultas |
| `carehub-historico` | `8082` | consumo de eventos de consultas, persistência do histórico e API GraphQL para consulta por paciente |
| `carehub-notificacao` | `8083` | consumo de eventos de consultas e geração de lembretes |

Infraestrutura auxiliar:

- PostgreSQL principal (`5432`) para o serviço de agendamento
- PostgreSQL de histórico (`carehub_historico_db`) criado via init script
- RabbitMQ (`5672`) com painel em `15672`

## Tecnologias utilizadas

- Java 21
- Spring Boot 4.1
- Spring Security
- Spring Web MVC
- Spring Data JPA
- Spring for GraphQL
- Spring AMQP
- MapStruct
- Flyway
- PostgreSQL
- H2 nos testes
- Docker e Docker Compose
- Postman

## Funcionalidades implementadas

### Segurança

- Autenticação HTTP Basic
- Perfis `MEDICO`, `ENFERMEIRO` e `PACIENTE`
- Autorização por perfil e por vínculo do paciente com seus próprios dados
- Respostas de erro em `ProblemDetail` para cenários HTTP

### Agendamento (`carehub-agendamento`)

- `GET /usuarios/me`
- `GET /consultas`
- `GET /consultas/{id}`
- `POST /consultas`
- `PUT /consultas/{id}`
- Configuração RabbitMQ apenas como produtor de eventos
- Publicação de evento no RabbitMQ quando uma consulta é criada ou alterada
- Validação de entrada para criação e atualização de consultas
- Regras de criação e atualização encapsuladas no domínio de `Consulta`

### Histórico (`carehub-historico`)

- Consumo assíncrono dos eventos publicados pelo agendamento
- Persistência idempotente do histórico de consultas
- Endpoint GraphQL em `/graphql`
- Interface GraphiQL em `/graphiql`
- Mapeamento de respostas com MapStruct
- Queries:
  - `historicoPorPaciente`
  - `consultasFuturasPorPaciente`

### Notificações (`carehub-notificacao`)

- Consumo assíncrono dos eventos publicados pelo agendamento
- Geração de lembretes a partir do evento recebido
- Envio atual via console/log
- Configuração RabbitMQ isolada como consumidor

## Organização interna

- Controllers e GraphQL controllers delegam transformação de DTOs para mappers MapStruct
- `ConsultaService` coordena o caso de uso sem concentrar montagem de evento
- `ConsultaEventFactory` centraliza a criação dos eventos publicados
- A entidade `Consulta` encapsula criação e atualização do agendamento
- As propriedades de mensageria foram separadas entre `producer` e `consumer`

## Como subir localmente

1. Suba a infraestrutura:

```powershell
docker compose up -d
```

2. Suba o serviço de agendamento:

```powershell
cd carehub-agendamento
mvnw.cmd spring-boot:run
```

3. Suba o serviço de histórico:

```powershell
cd carehub-historico
mvnw.cmd spring-boot:run
```

4. Suba o serviço de notificações:

```powershell
cd carehub-notificacao
mvnw.cmd spring-boot:run
```

## Como testar

### Collection Postman

Importe:

`docs/carehub.postman_collection.json`

Variáveis principais:

- `baseUrl = http://localhost:8081`
- `baseUrlHistorico = http://localhost:8082`

Credenciais de teste:

- `medico1 / 123456`
- `enfermeiro1 / 123456`
- `paciente1 / 123456`
- `paciente2 / 123456`
- `inativo1 / 123456`

Cobertura da collection:

- autenticação
- matriz de autorização REST
- queries GraphQL do histórico

### Cobertura automatizada dos testes (JaCoCo)

A execução dos testes foi validada com JaCoCo em cada serviço do monorepo:

| Serviço | Instruções | Linhas | Branches |
| --- | ---: | ---: | ---: |
| `carehub-agendamento` | 94,81% | 97,61% | 88,89% |
| `carehub-historico` | 82,16% | 82,83% | 61,54% |
| `carehub-notificacao` | 80,38% | 80,39% | 100,00% |
| `Consolidado` | 87,96% | 89,56% | 73,91% |

Observação: os testes de integração já estão estruturados no repositório com dependências declaradas e perfis dedicados. Para executá-los localmente com cobertura, é importante usar Java 21 no terminal/Maven e manter o RabbitMQ disponível.

## Endpoints e consultas

### REST — agendamento

- `GET /usuarios/me`
- `GET /consultas`
- `GET /consultas/{id}`
- `POST /consultas`
- `PUT /consultas/{id}`

Exemplo de payload para criar consulta:

```json
{
  "pacienteId": 1,
  "profissionalId": 1,
  "dataHora": "2030-09-15T10:00:00",
  "observacoes": "Consulta de rotina"
}
```

### GraphQL — histórico

Endpoint: `POST http://localhost:8082/graphql`

Exemplo:

```graphql
query {
  historicoPorPaciente(pacienteId: "1") {
    id
    dataHora
    status
    paciente {
      id
      nome
    }
    profissional {
      id
      nome
    }
  }
}
```

## Observações

- O `docker-compose.yml` sobe RabbitMQ e PostgreSQL.
- O serviço `carehub-historico` usa banco próprio.
- O `carehub-agendamento` publica eventos usando apenas `exchange` e `routing-key`.
- Os serviços consumidores mantêm suas próprias filas, DLQ e bindings.
- `target/` não deve ser considerado fonte de verdade do projeto.

## Relatório técnico

Arquivos gerados na raiz:

- `Relatorio Tecnico - CareHub API - Fase 3.md`
- `Relatorio Tecnico - CareHub API - Fase 3.html`
- `Relatorio Tecnico - CareHub API - Fase 3.pdf`

Para gerar HTML e PDF a partir do Markdown:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-report.ps1
```

Para monitorar o Markdown e regerar automaticamente ao salvar:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\watch-report.ps1
```
