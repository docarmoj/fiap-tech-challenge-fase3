# Demonstração da API com Postman

## Arquivos

- `carehub.postman_collection.json`: Collection com as requisições e testes automáticos.
- `carehub-local.postman_environment.json`: Environment local com URLs, credenciais de demonstração e IDs da carga inicial.

## Preparação

Na raiz do projeto, suba a infraestrutura:

```bash
docker compose up -d
```

Em terminais separados, inicie os serviços usados pela Collection:

```bash
cd carehub-agendamento
./mvnw spring-boot:run
```

```bash
cd carehub-historico
./mvnw spring-boot:run
```

O serviço `carehub-notificacao` pode ser iniciado para acompanhar os eventos e lembretes no console, mas ele não possui endpoint HTTP e não é necessário para executar as requisições.

## Importação e execução

1. No Postman, use **Import** e selecione os dois arquivos deste diretório.
2. Selecione o Environment **CareHub - Local**.
3. Execute requisições individuais ou abra a Collection e escolha **Run collection**.
4. No Runner, mantenha a ordem original das requisições.

A ordem importa na pasta **Matriz de autorizacao**: a requisição `MEDICO cria consulta` grava o ID retornado em `consultaCriada`; a requisição seguinte usa esse ID para demonstrar a alteração.

## Variáveis do Environment

| Variável | Valor local | Uso |
| --- | --- | --- |
| `baseUrl` | `http://localhost:8081` | API REST de agendamento |
| `baseUrlHistorico` | `http://localhost:8082` | API GraphQL de histórico |
| `medicoUsername` | `medico1` | Credencial de médico |
| `enfermeiroUsername` | `enfermeiro1` | Credencial de enfermeiro |
| `paciente1Username` | `paciente1` | Credencial do paciente 1 |
| `paciente2Username` | `paciente2` | Credencial do paciente 2 |
| `usuarioInativoUsername` | `inativo1` | Cenário de autenticação negada |
| `senha` | `123456` | Senha da carga de demonstração |
| `paciente1Id` / `paciente2Id` | `1` / `2` | IDs de pacientes da carga inicial |
| `profissionalMedicoId` / `profissionalEnfermeiroId` | `1` / `2` | IDs de profissionais da carga inicial |
| `consultaPaciente1` / `consultaPaciente2` | `1` / `3` | Consultas usadas nos cenários de autorização |

Se as portas, credenciais ou IDs forem alterados no projeto, basta atualizar o Environment; não é necessário editar a Collection.

## Cobertura demonstrada

| Pasta | Validações principais |
| --- | --- |
| `Autenticacao` | credencial ausente, senha incorreta, usuário inativo e identificação dos três perfis |
| `Matriz de autorizacao` | listagem e leitura como médico, enfermeiro e paciente; criação; alteração; filtro dos dados do paciente; respostas `403` e `404` |
| `Historico GraphQL (8082)` | histórico e consultas futuras permitidas, filtro por status, acesso a dados de outro paciente negado, contrato GraphQL e ausência de credencial |

Cada requisição possui testes na aba **Tests**. No Runner, o resultado esperado é que todos os testes fiquem verdes, inclusive aqueles que confirmam respostas `401`, `403` e `404`.
