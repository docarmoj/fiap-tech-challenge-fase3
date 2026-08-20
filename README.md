# CareHub API — Tech Challenge Fase 3 (FIAP)

Solução de backend modular para gerenciamento de agendamentos hospitalares, histórico de consultas via GraphQL e notificações assíncronas via mensageria.

Este projeto foi desenvolvido como requisito avaliativo da fase 3 do Tech Challenge da pós-graduação FIAP.

## 🏛️ Arquitetura do Sistema

O sistema foi estruturado seguindo o padrão **Monorepo**, contendo 3 microsserviços independentes que se comunicam de forma síncrona (REST/GraphQL) e assíncrona (RabbitMQ):

```text
                                +-----------------------+
                                |   Cliente / Postman   |
                                +-----------+-----------+
                                            |
                    +-----------------------+-----------------------+
                    | (HTTP / REST)                                 | (HTTP / GraphQL)
                    v                                               v
        +-----------------------+                       +-----------------------+
        |  carehub-agendamento  |                       |   carehub-historico   |
        |      (Porta 8081)     |                       |      (Porta 8082)     |
        +-----------+-----------+                       +-----------+-----------+
                    |                                               |
                    | (Publica Evento)                              | (Consulta Dados)
                    v                                               v
        +-----------------------+                       +-----------------------+
        |       RabbitMQ        |                       |   Banco de Dados      |
        |    (Exchange/Fila)    |                       |   (PostgreSQL/H2)     |
        +-----------+-----------+                       +-----------+-----------+
                    |
                    | (Consome Evento)
                    v
        +-----------------------+
        |  carehub-notificacao  |
        |      (Porta 8083)     |
        +-----------------------+

 ## 🛠️ Tecnologias Utilizadas
 * **Linguagem: Java 17+
 * **Framework: Spring Boot 3.x
 * **Módulos Spring: Spring Data JPA, Spring Security, Spring Web, Spring for GraphQL, Spring AMQP
 * **Mensageria: RabbitMQ
 * **Banco de Dados: H2 (em memória para desenvolvimento/testes) e PostgreSQL (via Docker)
* ** Ferramentas: Docker, Docker Compose, Maven, Flyway

# Banco de Dados
* ** Banco H2 em memória para desenvolvimento/testes
 * Para subir a aplicação localmente basta abrir no navegador o endereço: `http://localhost:8081/h2-console` e utilizar as credenciais:
   * JDBC URL: `jdbc:h2:mem:testdb`
   * User Name: `sa`
   * Password: (deixe em branco)