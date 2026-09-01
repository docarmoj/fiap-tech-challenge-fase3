# ADR 001 - Banco proprio do carehub-historico, alimentado por evento

Status: aceito
Data: 2026-08-31
Contexto: TASK OPCIONAL (servico de historico independente) e DEV TASK 07 (consulta com GraphQL)

## Contexto

O `carehub-historico` nasceu como esqueleto: entidades mapeando `tb_consulta` e `tb_profissional`, ou seja, lendo o banco do `carehub-agendamento`. O agendamento e o dono desse schema e o unico modulo que roda migrations sobre ele.

Manter essa leitura carona significaria que o historico nao sobe sem o banco do agendamento, e que qualquer migration do agendamento pode quebrar o historico em silencio, sem nada no codigo do historico indicando a dependencia. A TASK OPCIONAL pede explicitamente separacao entre os dois servicos e baixo acoplamento.

## Decisao

O `carehub-historico` tem banco proprio, `carehub_historico_db`, com migrations proprias, e e alimentado pelo evento `ConsultaEvent` que o agendamento ja publicava no RabbitMQ.

O evento passou a ser o unico contrato entre os modulos. Nao ha modulo compartilhado, dependencia de codigo nem banco em comum. O DTO do evento e duplicado de proposito em cada lado, e cada consumidor acompanha so os campos que usa.

A tabela `tb_consulta_historico` e desnormalizada: nome de paciente e nome de profissional sao colunas da propria linha, e nao relacoes. Isso e um read model no sentido de CQRS - existe para ser lido, nao para ser a verdade.

## Consequencias

**A favor**

- O historico sobe sozinho. Nao alcanca o banco do agendamento nem depende dele para autenticar: tem `tb_usuario` proprio, com as mesmas credenciais e a mesma matriz de perfis.
- Adicionar um consumidor nao toca no publicador. O exchange e direct e as duas filas - notificacao e historico - recebem o mesmo evento de forma independente, cada uma com sua DLQ.
- A leitura do GraphQL e um unico SELECT por query. Como os campos aninhados de paciente e de profissional saem de colunas da mesma linha, nao existe consulta adicional por item: o problema N+1, que e a armadilha classica de GraphQL sobre JPA, nao chega a existir.

**Contra**

- **Consistencia eventual.** Entre gravar a consulta no agendamento e a linha aparecer no historico existe uma janela: o tempo de publicacao, entrega e consumo da mensagem. Na pratica e da ordem de milissegundos, mas nao e zero, e uma leitura imediatamente apos a escrita pode nao encontrar o dado. Este e o custo real da decisao, e foi aceito: o historico e uma consulta de acompanhamento, nao um confirmador de escrita.
- **Dado duplicado.** O nome do paciente vive nos dois bancos. Renomear um paciente no agendamento so corrige o historico das consultas cujo evento for republicado. O sistema nao tem edicao de cadastro hoje.
- **Um consumidor a mais para operar.** Se o `carehub-historico` ficar fora do ar, as mensagens se acumulam na fila dele - o que e o comportamento desejado - mas alguem precisa observar a DLQ, o que ninguem faz hoje.

## Alternativa descartada

Banco compartilhado, com o historico lendo as tabelas do agendamento. Seria menos codigo e teria consistencia imediata, mas mantem os dois servicos amarrados no schema, que e exatamente o acoplamento que a tarefa pede para remover. Tambem exigiria join entre consulta, paciente e profissional a cada query do GraphQL, reintroduzindo o N+1 que o read model desnormalizado elimina.

## Limites conhecidos

Os limites aceitos nesta entrega - ordenacao de eventos concorrentes, enum de status duplicado, upsert dependente de concorrencia 1 - estao registrados em `spec-task-opcional-servico-historico.md`.
