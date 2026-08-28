# TASK OPCIONAL - Servico de historico independente

Tarefa: ClickUp (Task opcional - Criar Servico de Historico independente)
Branch: feature/task-opcional-historico-servico (a partir de main, apos o merge da DEV TASK 05)
Servicos: carehub-agendamento, carehub-notificacao, carehub-historico

## Problem Statement

O carehub-historico existe como modulo Maven independente, mas e um esqueleto: nao tem datasource, nao consome nada e nao expoe nada. Suas duas entidades mapeiam `tb_consulta` e `tb_profissional`, ou seja, pressupoem o banco do carehub-agendamento.

Manter esse pressuposto significa integracao por banco compartilhado: dois servicos acoplados ao mesmo schema fisico, sem contrato entre eles. Uma migration do agendamento quebra o historico em runtime, sem nenhum sinal em tempo de compilacao ou de teste. Duas das responsabilidades listadas na tarefa - manter separacao entre Agendamento e Historico, e integrar-se aos demais servicos - ficariam sem implementacao alguma: nao ha integracao a demonstrar quando um servico simplesmente le a tabela do outro.

A descricao da tarefa admite armazenar **ou** disponibilizar o historico, entao a leitura direta nao seria reprovada pela letra do criterio. Ela seria, porem, indefensavel no criterio de baixo acoplamento.

## Solution

O carehub-historico passa a ser dono dos proprios dados, alimentado de forma assincrona pelo agendamento.

- Banco proprio, com migrations proprias. O historico nao alcanca `tb_consulta`.
- Um read model desnormalizado, com uma linha por consulta, atualizada por evento.
- O evento de consulta ja publicado pelo agendamento e enriquecido com os campos que o historico precisa, e ganha uma segunda fila consumidora ao lado da fila de notificacao.
- O consumo e idempotente e tolerante a reentrega e a reordenacao.
- O historico autentica com credenciais proprias, replicando o modelo de perfil e vinculo da DEV TASK 05.

O acoplamento entre agendamento e historico passa a ser exclusivamente o contrato do evento.

## User Stories

1. Como avaliador do Tech Challenge, quero que o servico de historico suba sem o agendamento no ar, para verificar que ele e de fato independente.
2. Como avaliador do Tech Challenge, quero ver uma consulta criada no agendamento aparecer no banco do historico, para verificar a integracao assincrona ponta a ponta.
3. Como avaliador do Tech Challenge, quero que o historico tenha dados desde o primeiro boot, para conseguir consultar o historico sem precisar criar consultas antes.
4. Como avaliador do Tech Challenge, quero que o historico use as mesmas credenciais do agendamento, para nao precisar de uma segunda carga de usuarios na hora de testar.
5. Como desenvolvedor do time, quero que o historico nao consiga ler as tabelas do agendamento, para que o desacoplamento seja garantido pela infraestrutura e nao pela disciplina de quem escreve codigo.
6. Como desenvolvedor do time, quero que o evento carregue tudo que o historico precisa, para que o historico nunca precise chamar o agendamento de volta.
7. Como desenvolvedor do time, quero que o consumo do mesmo evento duas vezes nao duplique linha, para que a reentrega do broker nao corrompa o historico.
8. Como desenvolvedor do time, quero que um evento mais antigo nao sobrescreva um estado mais novo, para que reordenacao de mensagens nao regrida o dado.
9. Como desenvolvedor do time, quero que uma mensagem que falha repetidamente va para a DLQ, para que ela nao bloqueie a fila.
10. Como desenvolvedor do time, quero que o historico atualize a linha correspondente quando a consulta e alterada, para que o read model reflita o ultimo estado conhecido.
11. Como desenvolvedor do time, quero que a routing key nao carregue o nome de um consumidor especifico, para deixar claro que o evento descreve um fato e nao um destino.
12. Como desenvolvedor do carehub-notificacao, quero que meu DTO continue funcionando quando o evento ganha campos, para nao precisar de release coordenado com o agendamento.
13. Como desenvolvedor do time, quero que o modulo de historico nao carregue classes escritas para a arquitetura descartada, para que ninguem leia codigo morto como se fosse decisao vigente.
14. Como Medico ou Enfermeiro, quero autenticar no servico de historico, para acessar os dados de historico.
15. Como Paciente, quero autenticar no servico de historico, para acessar o meu historico.
16. Como usuario nao autenticado, quero receber 401 no servico de historico, para que meu cliente saiba pedir credenciais.
17. Como usuario inativo, quero continuar barrado no login do historico, para que credencial desativada nao ganhe permissao em nenhum servico.
18. Como usuario autenticado do historico, quero que meu vinculo com paciente ou profissional esteja disponivel na sessao, para que as regras de acesso da DEV TASK 07 possam ser aplicadas.
19. Como responsavel pela demonstracao, quero criar uma consulta ao vivo e ve-la surgir no historico, para provar que o pipeline assincrono funciona de verdade.

## Implementation Decisions

### O evento e enriquecido; nao se cria um segundo evento

O evento de consulta hoje carrega identificador da consulta, identificador e nome e email do paciente, data e hora, e a acao. Ganha identificador e nome do profissional, status, observacoes e um carimbo de quando o fato ocorreu.

O evento descreve o que aconteceu com a consulta, nao o que cada consumidor quer ler. Publicar um segundo evento especifico para o historico dobraria a superficie de falha na publicacao - duas mensagens por gravacao, sem transacao entre elas - para modelar o mesmo fato duas vezes.

### O DTO do carehub-notificacao permanece como esta

Os DTOs de evento sao duplicados de proposito entre publisher e consumer. O listener de notificacao nao usa nenhum dos campos novos, entao ele continua declarando so o que consome. Um teste prova que a desserializacao ignora campos desconhecidos, transformando essa afirmacao em evidencia verificavel.

Espelhar os campos nos dois DTOs recriaria exatamente o acoplamento que a duplicacao existe para evitar.

### Routing key renomeada

A routing key deixa de se chamar `consulta.notificacao` e passa a nomear o fato, nao o destino. Com dois consumidores, o nome antigo amarra o evento a um dos lados e sugere que o historico esta pendurado na fila da notificacao.

A exchange continua direct e mantem o nome. Trocar para topic com chaves por acao seria mais expressivo, mas mudaria o tipo de uma exchange ja declarada e testada em dois modulos, sem ganho para os criterios de aceite.

### Topologia da mensageria

O historico declara as proprias beans de exchange, fila, binding e DLQ, como os outros modulos ja fazem. A fila do historico e bindada na mesma exchange e mesma routing key da fila de notificacao; como a exchange e direct, as duas filas recebem cada mensagem publicada.

Os nomes ficam no namespace de configuracao ja existente no projeto, com a mesma consequencia ja documentada: os nomes estao duplicados entre modulos e mudar de um lado exige mudar do outro.

O historico replica a configuracao de retry e DLQ que o notificacao ja usa.

### Read model desnormalizado

A tabela de historico guarda uma linha por consulta, com o identificador da consulta como chave natural unica, mais identificador e nome do paciente, identificador e nome do profissional, data e hora, status, observacoes e o instante da ultima atualizacao.

Nao se replicam as tabelas de paciente e de profissional. Desnormalizar e o padrao de um read model: cada leitura vira um SELECT unico, sem risco de N+1 nos campos aninhados que a DEV TASK 07 vai expor, e o historico deixa de precisar sincronizar cadastro de paciente e de profissional - que o agendamento nem publica.

A consequencia aceita e que o nome gravado no historico e o nome vigente no momento do evento. Para um historico de atendimentos isso e o comportamento desejado, nao um defeito.

### Consumo idempotente e tolerante a reordenacao

O listener faz upsert pela chave natural da consulta: encontra a linha e atualiza, ou insere. Uma mensagem reentregue nao duplica.

Alem disso, o listener descarta o evento cujo carimbo de ocorrencia seja anterior a ultima atualizacao ja gravada. Com um consumidor e concorrencia 1 a ordem ja seria preservada pelo broker, mas essa garantia nao aparece em lugar nenhum do codigo; com o carimbo, a correcao fica explicita e testavel.

Nao existe evento de exclusao, porque o agendamento nao tem endpoint DELETE. O listener nunca remove linha.

### Autenticacao propria, replicando o modelo da DEV TASK 05

O historico ganha uma tabela de usuarios no proprio banco, com a mesma restricao de coerencia entre role e vinculo, e replica o principal autenticado, o servico de detalhes de usuario, a configuracao de seguranca, o entry point de autenticacao e o handler de acesso negado. A carga inicial repete os mesmos cinco usuarios do agendamento, com as mesmas senhas.

Usuarios em memoria foram descartados: sem vinculo com paciente no usuario nao existe regra de vinculo, e o criterio de regras de acesso da DEV TASK 07 cai junto.

Chamar o agendamento para validar credencial reintroduziria acoplamento em runtime. Um token assinado emitido pelo agendamento seria o desenho correto em producao, mas o Tech Challenge pede autenticacao basica e isso seria uma tarefa inteira. A duplicacao fica registrada aqui como limite conhecido, nao como descuido.

### Segundo database no mesmo container Postgres

O compose ganha um script de inicializacao criando o database do historico no Postgres ja existente. O historico aponta para ele e roda o proprio Flyway; o agendamento continua dono exclusivo do database original e segue sendo o unico a rodar migrations la.

Um segundo container Postgres daria o mesmo isolamento ao custo de mais um servico rodando na maquina de quem avalia. Schema separado dentro do mesmo database foi descartado: deixaria join entre os dois lados tecnicamente possivel, o que anula o argumento de desacoplamento.

### Dados de demonstracao vem de migration propria

A carga inicial do historico insere as mesmas quatro consultas da carga do agendamento, com os mesmos identificadores de consulta, mais os cinco usuarios. Assim os criterios de aceite tem dado para exibir desde o primeiro boot, inclusive em teste.

Backfill lendo o agendamento no boot foi descartado: reintroduz chamada sincrona entre os servicos so para popular tabela.

Na apresentacao, o seed cobre o caso estatico e a criacao de uma consulta ao vivo demonstra o pipeline assincrono.

### Faxina no modulo

A entidade de consulta e remapeada para a tabela de historico com os campos desnormalizados; a entidade de profissional e removida, porque deixa de existir como tabela; o repositorio e reescrito; o console H2 sai do escopo de compilacao e o H2 fica so em teste.

As classes atuais foram escritas para a arquitetura de banco compartilhado e nao sobrevivem a nenhuma parte desta decisao. Adaptar aos poucos deixaria codigo morto que o revisor teria que perguntar sobre.

## Testing Decisions

### O que faz um bom teste aqui

O teste afirma o que um observador externo ve: dado um evento entregue, qual passa a ser o estado do historico. Nao afirma como o listener chegou la. Trocar upsert manual por merge do JPA nao deve quebrar nenhum teste; mudar o efeito de um evento deve quebrar exatamente um.

### Seam

O mesmo seam ja usado no agendamento, e nenhum novo: aplicacao carregada com o profile de teste, H2 em modo PostgreSQL rodando as migrations reais do Flyway.

Nenhum broker sobe na suite. O listener e exercitado chamando o metodo diretamente com o evento, que e precisamente o que o broker faria - o mesmo espirito da decisao ja vigente no agendamento de substituir o publisher por mock.

Testes que gravam levam rollback transacional, pela mesma razao ja documentada no projeto: sem isso o estado vaza entre classes, e os identificadores da carga inicial sao usados de forma literal nos asserts.

### Cobertura

- Evento de criacao grava uma linha com todos os campos do read model.
- Evento de alteracao para a mesma consulta atualiza a linha existente, sem inserir outra.
- O mesmo evento entregue duas vezes deixa uma linha so.
- Evento com carimbo anterior a ultima atualizacao gravada nao altera a linha.
- Evento para uma consulta que ja veio na carga inicial atualiza a linha da carga.
- No carehub-notificacao: o DTO desserializa uma mensagem com campos extras sem erro.
- No carehub-agendamento: o publisher envia o evento com os campos novos preenchidos, em criacao e em alteracao.
- Autenticacao: usuario valido autentica, usuario inativo e barrado, requisicao sem credencial recebe 401.

## Out of Scope

- Schema GraphQL, queries e resolvers: DEV TASK 07.
- Regras de autorizacao sobre os dados de historico: DEV TASK 07. Esta tarefa entrega autenticacao e o principal com vinculo; quem consome e a 07.
- Sincronizacao de cadastro de paciente e de profissional por evento.
- Exclusao de consulta no historico: nao existe DELETE no agendamento.
- Backfill de dados historicos a partir do agendamento.
- Troca de autenticacao basica por token assinado ou provedor de identidade compartilhado.
- Mudar a exchange de direct para topic.
- Metrica, tracing ou log estruturado do consumo.
- Envio real de notificacao: continua sendo DEV TASK 11.

## Further Notes

- Esta tarefa e pre-requisito tecnico da DEV TASK 07 e deve ser mergeada antes dela. Entregar a 07 sobre banco compartilhado significaria desmontar a 07 depois.
- A branch depende do merge da DEV TASK 05: o historico replica o servico de autorizacao e o modelo de principal entregues por ela, e ambas tocam o service de consulta no agendamento.
- A tarefa estava sem dono no board e foi assumida junto com a DEV TASK 07, porque quem faz a 07 e quem paga o preco de faze-las fora de ordem.
- Ordem de execucao, de fora para dentro: (1) enriquecer o evento com os campos novos e o carimbo de ocorrencia, mais o teste do publisher; (2) teste no notificacao provando tolerancia a campo desconhecido; (3) compose com o segundo database; (4) migrations do historico; (5) entidade e repositorio; (6) configuracao de mensageria e listener idempotente; (7) testes do listener; (8) seguranca. O contrato do evento vem primeiro porque atravessa tres modulos: se estiver errado, todo o resto e retrabalho.
- Pronto para abrir o PR quando a suite estiver verde nos tres modulos e a verificacao manual ponta a ponta tiver sido feita com Docker de pe: criar consulta no agendamento e ver a linha aparecer na tabela de historico. A suite nao cobre o pipeline real - o broker nao sobe em teste e o publisher e mockado - entao sem essa verificacao o PR seria aberto sem ninguem nunca ter visto uma mensagem real trafegar.
- A renomeacao da routing key exige subir os tres servicos juntos. Como nada e persistido e o ambiente e recriado do zero, nao ha migracao a fazer, mas vale um aviso no grupo.
- O README do projeto ja esta desatualizado em outros pontos; esta tarefa acrescenta um terceiro database e um segundo consumidor, que precisam entrar na documentacao de arquitetura.
