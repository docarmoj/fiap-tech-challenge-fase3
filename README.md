# CareHub API — Tech Challenge Fase 3

Backend modular para agendamento de consultas, histórico do paciente e notificações assíncronas em ambiente hospitalar, com autenticação HTTP Basic e controle de acesso por perfil.

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
- Publicação de evento no RabbitMQ quando uma consulta é criada ou alterada
- Validação de entrada para criação e atualização de consultas

### Histórico (`carehub-historico`)

- Consumo assíncrono dos eventos publicados pelo agendamento
- Persistência idempotente do histórico de consultas
- Endpoint GraphQL em `/graphql`
- Interface GraphiQL em `/graphiql`
- Queries:
  - `historicoPorPaciente`
  - `consultasFuturasPorPaciente`

### Notificações (`carehub-notificacao`)

- Consumo assíncrono dos eventos publicados pelo agendamento
- Geração de lembretes a partir do evento recebido
- Envio atual via console/log

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
