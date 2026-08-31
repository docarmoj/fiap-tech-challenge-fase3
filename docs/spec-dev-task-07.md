# DEV TASK 07 - Consulta do historico medico com GraphQL

Tarefa: ClickUp (DEV TASK 07 - Implementar consulta do historico medico com GraphQL)
Branch: feature/task07-graphql-historico (a partir de feature/task-opcional-historico-servico)
Servico: carehub-historico

## Problem Statement

Depois da task opcional, o carehub-historico tem os dados de atendimento, mas nada os expoe: o servico nao publica nenhum endpoint de leitura. O historico existe e ninguem consegue le-lo.

A API REST do agendamento tambem nao resolve o problema. Ela responde perguntas fixas, decididas pelo servidor: lista as consultas do usuario ou busca uma por id. Nao ha como pedir so os atendimentos futuros de um paciente, nem escolher quais campos vem na resposta. Cada nova pergunta exigiria um endpoint novo ou um parametro de query novo, e a resposta sempre carrega tudo, precise o cliente ou nao.

O Tech Challenge pede explicitamente consultas flexiveis sobre o historico medico, com duas perguntas minimas: todos os atendimentos de um paciente, e somente os futuros. Sem uma camada de consulta, nenhum criterio de aceite da tarefa pode ser demonstrado.

## Solution

O carehub-historico expoe um endpoint GraphQL unico e autenticado sobre o read model.

- Um schema tipado descreve os dados de atendimento e as perguntas possiveis. O cliente escolhe quais campos quer receber.
- Duas queries nomeadas cobrem as duas perguntas minimas da tarefa, e ambas aceitam um filtro opcional por status.
- Toda consulta exige o identificador do paciente, e o acesso e decidido em duas camadas - perfil e vinculo - replicando a matriz da DEV TASK 05.
- Uma interface interativa de exploracao do schema fica disponivel, atras de autenticacao, servindo de documentacao viva e de ferramenta de demonstracao.

## User Stories

1. Como Medico, quero listar todos os atendimentos de qualquer paciente, para avaliar o historico clinico antes de um atendimento.
2. Como Medico, quero listar somente os atendimentos futuros de qualquer paciente, para preparar a agenda de acompanhamento.
3. Como Enfermeiro, quero listar todos os atendimentos de qualquer paciente, para organizar o fluxo de atendimento.
4. Como Enfermeiro, quero listar somente os atendimentos futuros de qualquer paciente, para preparar a chegada do paciente.
5. Como Paciente, quero listar todos os meus atendimentos, para acompanhar meu proprio historico clinico.
6. Como Paciente, quero listar somente os meus atendimentos futuros, para saber o que ainda esta por vir.
7. Como Paciente, quero ser bloqueado ao pedir o historico de outro paciente, para que o sistema proteja o sigilo clinico.
8. Como Paciente, quero que a negativa seja explicita e nao um resultado vazio, para nao concluir por engano que nao tenho atendimentos.
9. Como qualquer usuario autenticado, quero filtrar os atendimentos por status, para separar agendados, realizados e cancelados sem filtrar no cliente.
10. Como qualquer usuario autenticado, quero que um status invalido seja rejeitado pelo proprio contrato, para descobrir o erro sem precisar interpretar mensagem de servidor.
11. Como qualquer usuario autenticado, quero ver o nome do profissional junto do atendimento, para saber quem atendeu sem uma segunda consulta.
12. Como qualquer usuario autenticado, quero ver o nome do paciente junto do atendimento, para conferir de quem e o historico que estou lendo.
13. Como consumidor da API, quero pedir apenas os campos que vou usar, para nao trafegar dado clinico desnecessario.
14. Como consumidor da API, quero que datas venham em um tipo declarado no schema, para nao precisar adivinhar o formato de serializacao.
15. Como consumidor da API, quero que um campo inexistente na query seja rejeitado antes de qualquer acesso a dados, para receber erro de contrato e nao erro de servidor.
16. Como consumidor da API, quero que a negativa de acesso traga a mesma mensagem que o REST do agendamento traz, para tratar o erro em um unico ponto do meu cliente.
17. Como consumidor da API, quero que a negativa por perfil e a negativa por vinculo sejam indistinguiveis, para que o sistema nao revele a existencia de dado alheio.
18. Como usuario nao autenticado, quero ser barrado no endpoint GraphQL, para que o historico clinico nao fique aberto.
19. Como avaliador do Tech Challenge, quero abrir uma interface interativa e navegar o schema, para entender o contrato sem ler codigo.
20. Como avaliador do Tech Challenge, quero uma colecao executavel cobrindo as queries e as negativas, para verificar os criterios de aceite sem subir a aplicacao na mao.
21. Como avaliador do Tech Challenge, quero que os dados retornados correspondam ao paciente solicitado, para verificar que o filtro acontece de fato.
22. Como desenvolvedor do time, quero que listar muitos atendimentos nao dispare uma consulta por atendimento, para que a leitura nao degrade com o volume.
23. Como desenvolvedor do time, quero que a regra de perfil fique visivel na assinatura do resolver, para saber quem pode chama-lo sem ler o service.
24. Como desenvolvedor do time, quero que a regra que depende do dado fique em um unico servico de autorizacao, para nao espalhar a checagem pelos resolvers.
25. Como desenvolvedor do time, quero uma suite que exercite cada celula da matriz de permissoes pelas queries, para verificar o requisito de seguranca sem subir a aplicacao.

## Implementation Decisions

### Contrato do schema

O schema e a decisao central desta tarefa e vale inline, porque nenhuma prosa o descreve com a mesma precisao:

```graphql
scalar DateTime

enum StatusConsulta { AGENDADA REALIZADA CANCELADA }

type Consulta {
  id: ID!
  dataHora: DateTime!
  status: StatusConsulta!
  observacoes: String
  paciente: Paciente!
  profissional: Profissional!
}

type Paciente     { id: ID!  nome: String! }
type Profissional { id: ID!  nome: String! }

type Query {
  historicoPorPaciente(pacienteId: ID!, status: StatusConsulta): [Consulta!]!
  consultasFuturasPorPaciente(pacienteId: ID!, status: StatusConsulta): [Consulta!]!
}
```

Os tipos aninhados de paciente e de profissional sao montados a partir das colunas da linha desnormalizada do read model. Nao ha join nem relacao JPA por tras deles.

### Duas queries nomeadas, nao uma query com flag

Cada criterio de aceite da tarefa corresponde a uma query com nome proprio. Uma unica query com um booleano de "apenas futuras" seria mais idiomatica, mas obrigaria o avaliador a inferir que os dois criterios estao cobertos. A flexibilidade que a tarefa pede fica no argumento opcional de status, presente nas duas.

### Definicao de atendimento futuro

Futuro e data e hora maior que o instante da consulta, independente de status. E a leitura literal do enunciado, e o repositorio do modulo ja expressa esse predicado. Restringir a agendados sairia do que a tarefa pede e esconderia dentro da query uma regra que o argumento de status ja permite expressar por fora.

### Data e hora como escalar declarado

GraphQL tem cinco escalares nativos e nenhum deles e data. O escalar `DateTime` e declarado no schema e registrado no runtime wiring.

Serializar data como texto nao custaria nada, mas deixaria o formato implicito no contrato e a validacao de entrada por conta do resolver. Com o escalar declarado, o schema se autodocumenta e qualquer filtro por periodo adicionado depois valida a entrada de graca.

**Correcao durante a implementacao.** A decisao original era usar a biblioteca de escalares estendidas do graphql-java. Ela nao serve a este modelo: o `ExtendedScalars.DateTime` coage apenas `OffsetDateTime` e `ZonedDateTime`, e lanca excecao de serializacao para qualquer outro tipo. O read model guarda `LocalDateTime`, porque a consulta e marcada em hora local e a coluna do Postgres e `timestamp` sem fuso.

As duas saidas eram converter o dado para `OffsetDateTime` na borda, inventando um fuso que o dominio nao tem e fazendo a resposta do GraphQL divergir da resposta REST do agendamento, ou declarar o escalar proprio. Foi declarado o escalar proprio, sobre `DateTimeFormatter.ISO_LOCAL_DATE_TIME`, o mesmo formato que o REST ja devolve. A dependencia nova deixou de existir: ela so entraria para fornecer um escalar que nao serviria.

### Status como enum do schema

O status entra como enum, nao como texto livre. O contrato passa a rejeitar valor invalido antes de alcancar o resolver, e a interface interativa oferece os valores validos no autocomplete. E o tipo de garantia que o REST so consegue com validacao escrita a mao.

### Identificador do paciente sempre obrigatorio

As duas queries exigem o identificador do paciente, inclusive quando quem chama e o proprio paciente. Isso casa com o criterio de aceite - os dados retornados correspondem ao paciente solicitado - e evita que o cliente receba silenciosamente um resultado diferente do que pediu.

Quando um Paciente informa identificador que nao e o seu, a resposta e negativa de acesso, nunca lista vazia e nunca "nao encontrado". E a mesma semantica que a DEV TASK 05 ja cravou no REST, onde consulta de outro paciente responde 403 e nao 404, pela mesma razao deliberada: o criterio pede bloqueio observavel.

### Autorizacao em duas camadas, replicando a DEV TASK 05

A regra que depende apenas do perfil vive como anotacao de autorizacao no metodo do resolver. A regra que depende do dado - este usuario pode ver o historico deste paciente? - vive em um servico de autorizacao do modulo, replicando o do agendamento e mantendo o padrao deny-by-default: uma lista enumera os perfis com acesso total e qualquer perfil fora dela cai em acesso negado.

Expressar a regra de vinculo como expressao SpEL dentro da anotacao foi descartado pelo mesmo motivo ja registrado na DEV TASK 05: esconde regra de negocio em uma string que nenhuma ferramenta refatora e nenhum teste alcanca isoladamente.

Matriz de permissoes das duas queries:

| Query | Medico | Enfermeiro | Paciente |
|---|---|---|---|
| historicoPorPaciente | qualquer paciente | qualquer paciente | so o proprio |
| consultasFuturasPorPaciente | qualquer paciente | qualquer paciente | so o proprio |

### Formato do erro de acesso negado

Em GraphQL a resposta e sempre HTTP 200: o campo pedido vem nulo e o motivo vai em um array de erros, com uma classificacao. O Spring ja registra automaticamente um resolver que converte a excecao de acesso negado do Spring Security em erro classificado como proibido, entao a classificacao correta sai sem codigo.

Ainda assim, um resolver de excecao proprio ajusta a mensagem para a mesma que o handler REST devolve. A razao esta registrada na DEV TASK 05: as duas negativas - por perfil e por vinculo - precisam ser indistinguiveis, e responder textos diferentes em REST e GraphQL para a mesma regra e inconsistencia visivel na colecao de testes.

### Sem N plus 1, por construcao do read model

Cada query e um unico SELECT sobre a tabela desnormalizada. Os campos aninhados de paciente e profissional saem de colunas da mesma linha, entao nao existe consulta adicional por item e nao ha necessidade de DataLoader nem de carregamento antecipado.

Essa e a razao pratica de o read model da task opcional ser desnormalizado, e vale registrar aqui porque o problema N mais 1 e a armadilha classica de GraphQL sobre JPA.

### Endpoint e interface interativa, ambos autenticados

O endpoint GraphQL exige autenticacao, como qualquer outra rota do servico. A interface interativa de exploracao fica habilitada e tambem autenticada - ela e a melhor ferramenta de apresentacao disponivel e serve de documentacao viva, sem abrir buraco de acesso.

Habilita-la apenas em um profile de desenvolvimento foi descartado: quem avalia roda o projeto com a configuracao padrao.

### Faixa de filtragem sempre no banco

O filtro por paciente, por futuro e por status acontece na consulta ao banco, nunca em memoria. E a mesma decisao ja registrada na DEV TASK 05, e pela mesma razao: alem do custo, filtrar depois deixa dado clinico de outro paciente transitando pela aplicacao.

## Testing Decisions

### O que faz um bom teste aqui

O teste dispara uma query real e afirma o que um cliente observa: os dados retornados e, quando o acesso e negado, a presenca do erro classificado. Nao afirma qual camada tomou a decisao, nem se a regra veio de anotacao ou de servico. Trocar a implementacao de uma query nao deve quebrar teste; mudar quem pode ver o que deve quebrar exatamente um.

Os asserts sobre resultado verificam os identificadores retornados, nao apenas a quantidade - a diferenca entre "filtrou" e "retornou o numero certo de linhas erradas".

### Seam

O mesmo seam ja usado no projeto, acrescido do tester de GraphQL: aplicacao carregada com o profile de teste, H2 em modo PostgreSQL rodando as migrations reais do Flyway, e as queries disparadas pelo tester. Nenhum seam novo.

O usuario autenticado nos testes e o principal real do servico, construido a partir da entidade de usuario, e nunca um mock generico de usuario com roles: as regras de vinculo leem o identificador do paciente de dentro do principal, informacao que o mock nao carrega. Essa e a mesma decisao ja registrada nos testes do agendamento.

Prior art no repositorio: os testes de autorizacao de consulta e de autenticacao basica do carehub-agendamento, que montam o principal da mesma forma e afirmam status e corpo pela borda.

Um slice isolado de GraphQL com repositorio mockado foi descartado: nao exercita nem as migrations nem a seguranca, que e exatamente onde moram os criterios de aceite desta tarefa.

### Cobertura

Um teste por celula relevante da matriz, mais o contrato:

- Medico consulta o historico de qualquer paciente e recebe os atendimentos daquele paciente.
- Enfermeiro consulta o historico de qualquer paciente e recebe os atendimentos daquele paciente.
- Paciente consulta o proprio historico e recebe apenas os proprios atendimentos.
- Paciente consultando historico de outro paciente recebe erro classificado como proibido, com dado nulo.
- A query de futuros devolve somente o atendimento com data posterior ao instante atual, para o mesmo paciente cujo historico completo traz tambem o passado.
- O filtro por status restringe o resultado dentro de cada uma das duas queries.
- Status invalido e rejeitado como erro de validacao do schema, antes de qualquer acesso a dados.
- Requisicao sem credencial e barrada no endpoint GraphQL.
- A mensagem da negativa e a mesma nas duas origens de negativa.

Os dados vem da carga inicial do proprio historico, entregue pela task opcional - uma consulta passada e uma futura por paciente. Nenhuma migration nova nesta tarefa.

## Out of Scope

- Mutations GraphQL: criacao e alteracao de consulta continuam sendo REST no carehub-agendamento.
- Qualquer escrita no historico por outra via que nao o evento.
- Paginacao, ordenacao e filtro por periodo nas queries.
- Subscriptions, federation, persisted queries e limite de profundidade ou complexidade de query.
- DataLoader e cache de leitura: desnecessarios sobre o read model desnormalizado.
- Expor o historico tambem por REST.
- Troca de autenticacao basica por token assinado.
- Envio real de notificacao: DEV TASK 11.

## Further Notes

- Esta tarefa depende do merge da task opcional de servico de historico, que entrega o banco proprio, o read model, o pipeline de eventos e a autenticacao do modulo. Sem ela, esta spec nao tem de onde ler.
- A matriz de permissoes replicada aqui e a mesma definida na DEV TASK 05. Foi esse acoplamento que motivou fazer a 05 antes da 07.
- A colecao do Postman ganha uma pasta de historico GraphQL dentro da colecao existente, e nao uma colecao separada - o Tech Challenge pede colecao para testar a API, e quebrar em duas faria o avaliador rodar duas coisas. Os asserts precisam olhar o corpo da resposta, porque em GraphQL o status e sempre 200 mesmo quando o acesso e negado.
- Vale um documento curto de decisao de arquitetura registrando o banco proprio do historico e o trade-off da consistencia eventual. A documentacao de arquitetura e item avaliado, e essa e a decisao mais interessante que o grupo tomou.
- Pronto para abrir o PR quando a suite do historico estiver verde e a pasta nova da colecao rodar inteira no Collection Runner.
