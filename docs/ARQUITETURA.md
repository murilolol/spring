# Arquitetura do Sistema

Este documento descreve detalhadamente a arquitetura de software, o modelo de dados relacional, a topologia de camadas, as máquinas de estado e as decisões de engenharia adotadas na API **Restaurante 2026**.

Para a especificação dos endpoints HTTP, consulte [API.md](API.md). Para o detalhamento do domínio de negócio, consulte [tema-do-projeto.md](tema-do-projeto.md).

> **TL;DR** — A aplicação adota Clean Architecture em camadas estritas baseadas em Domain-Driven Design (DDD). O banco de dados PostgreSQL 16 é evoluído por migrações incrementais via Liquibase com `ddl-auto=validate`. A consistência das regras de negócio é protegida por entidades ricas no domínio e máquinas de estado explícitas.

<br>

## Índice

- [Visão geral da topologia](#visão-geral-da-topologia)
- [Divisão em camadas e pacotes](#divisão-em-camadas-e-pacotes)
- [Modelo relacional e entidades](#modelo-relacional-e-entidades)
- [Máquinas de estado](#máquinas-de-estado)
- [Gerenciamento de schema com Liquibase](#gerenciamento-de-schema-com-liquibase)
- [Segurança e controle de acesso](#segurança-e-controle-de-acesso)
- [Exemplo de ciclo de vida HTTP](#exemplo-de-ciclo-de-vida-http)
- [Perfis de ambiente](#perfis-de-ambiente)
- [Decisões de engenharia](#decisões-de-engenharia)

---

## Visão geral da topologia

O sistema separa estritamente a camada de apresentação HTTP, a camada de serviço de aplicação e a persistência relacional do **modelo de domínio puro**.

```mermaid
flowchart LR
    subgraph Client["Cliente HTTP"]
        C["curl / Postman / Client Web / Mobile"]
    end

    subgraph Server["Servidor Spring Boot 3.4"]
        SEC["Spring Security\n(HTTP Basic)"]
        API["Controllers REST\n+ DTOs"]
        SVC["Services Transacionais\n(@Transactional)"]
        DOM["Modelo de Domínio\n(Entidades Ricas)"]
        REPO["Spring Data JPA\n(Repositories)"]
    end

    subgraph DB["Infraestrutura de Banco"]
        PG[(PostgreSQL 16)]
        LB["Liquibase\n(16 Migrações YAML)"]
    end

    C -- "HTTP Basic Header" --> SEC
    SEC --> API
    API --> SVC
    SVC --> DOM
    SVC --> REPO
    REPO -- "JDBC / SQL" --> PG
    LB -- "ddl-auto=validate" --> PG
```

---

## Divisão em camadas e pacotes

O código-fonte é organizado sob a raiz `com.curso.restaurante`:

```text
com.curso.restaurante
├── config/            Configuration, SecurityConfig, Handlers RFC 7807, Bootstrap Admin
├── api/               Controllers REST, DTOs de Request/Response e validações (@Valid)
├── domain/            Entidades ricas, invariantes de negócio, enums e exceções
├── repository/        Interfaces Spring Data JPA e JPQL queries
└── service/           Serviços de aplicação, orquestração e controle transacional
```

```mermaid
flowchart TD
    subgraph api ["Camada API (Presentation)"]
        Controller["Controllers REST (@RestController)"]
        DTO["DTOs (Request / Response)\nBean Validation (@Valid)"]
    end

    subgraph service ["Camada Service (Application)"]
        Service["Services (@Service)\n@Transactional"]
    end

    subgraph domain ["Camada Domain (Business)"]
        Entity["Entidades Ricas (Java puro + JPA)\nRegras & Invariantes"]
        Enum["Enums & Máquinas de Estado"]
    end

    subgraph repository ["Camada Repository (Infrastructure)"]
        Repo["Spring Data JPA Interfaces"]
    end

    Controller --> DTO
    Controller --> Service
    Service --> Entity
    Service --> Repo
    Repo --> DB[(PostgreSQL)]
```

### Responsabilidades por camada

| Camada | Responsabilidade | Restrições |
| :--- | :--- | :--- |
| **`api`** | Tradução HTTP ↔ DTO, validações de contrato (`@Valid`), serialização JSON | Proibido conter regras de negócio ou chamadas SQL diretas |
| **`service`** | Orquestração de serviços, transações (`@Transactional`), regras que cruzam agregados | Proibido acessar o banco sem passar pelas interfaces Repository |
| **`domain`** | Entidades ricas, invariantes de construtor, cálculos com `BigDecimal`, enums | Proibido depender de anotações Spring Web ou Spring Security |
| **`repository`** | Interfaces JPA para persistência e recuperação de dados no PostgreSQL | Proibido conter decisões de regras de negócio |

---

## Modelo relacional e entidades

O banco de dados é composto por 12 tabelas principais gerenciadas pelo Liquibase:

```mermaid
erDiagram
    USUARIO ||--o{ COMANDA : "abre"
    USUARIO ||--o{ PEDIDO : "lanca"
    USUARIO ||--o{ PREPARO_ITEM : "prepara"
    USUARIO ||--o{ SESSAO_CAIXA : "opera"
    USUARIO ||--o{ SANGRIA : "registra"
    USUARIO ||--o{ PAGAMENTO : "registra"
    CATEGORIA_CARDAPIO ||--o{ ITEM_CARDAPIO : "classifica"
    CLIENTE ||--o{ COMANDA : "possui"
    MESA ||--o{ COMANDA : "hospeda"
    COMANDA ||--o{ PEDIDO : "agrupa"
    COMANDA ||--o{ PAGAMENTO : "quita"
    PEDIDO ||--o{ ITEM_PEDIDO : "contem"
    ITEM_CARDAPIO ||--o{ ITEM_PEDIDO : "referencia"
    ITEM_PEDIDO ||--|| PREPARO_ITEM : "enfileira"
    SESSAO_CAIXA ||--o{ SANGRIA : "registra"
    SESSAO_CAIXA ||--o{ PAGAMENTO : "concentra"
```

---

## Máquinas de estado

A integridade do fluxo de atendimento é garantida por transições orientadas a estado finitos. Transições inválidas disparam `TransicaoDeStatusInvalidaException` (HTTP 409 Conflict).

### 1. Ciclo de vida do pedido (`StatusPedido`)

```mermaid
stateDiagram-v2
    [*] --> ABERTO: Lançamento na comanda
    ABERTO --> EM_PREPARO: Início do preparo na cozinha
    EM_PREPARO --> PRONTO: Conclusão do prato
    PRONTO --> ENTREGUE: Entrega na mesa pelo garçom
    ENTREGUE --> PAGO: Comanda quitada no caixa (cascata)

    ABERTO --> CANCELADO: Cancelamento com motivo
    EM_PREPARO --> CANCELADO: Cancelamento excepcional
    PRONTO --> CANCELADO: Cancelamento excepcional

    PAGO --> [*]
    CANCELADO --> [*]
```

`PAGO` não tem endpoint próprio — é atingido apenas por cascata quando o pagamento da
Comanda quita o total (`Comanda.marcarComoPaga()` chama `Pedido.marcarComoPago()` em cada
pedido `ENTREGUE`). Um pedido `ENTREGUE` não pode mais ser cancelado.

### 2. Ciclo de vida da comanda (`StatusComanda`)

```mermaid
stateDiagram-v2
    [*] --> ABERTA: Comanda vinculada à mesa/cliente
    ABERTA --> FECHADA: Solicitação do fechamento de conta
    FECHADA --> ABERTA: Reabertura (correção antes do pagamento)
    FECHADA --> PAGA: Pagamento quita o saldo devedor no caixa
    ABERTA --> CANCELADA: Cancelamento sem consumo cobrável

    PAGA --> [*]
    CANCELADA --> [*]
```

### 3. Ciclo de vida da mesa (`StatusMesa`)

`ocupar()`/`liberar()` não têm endpoint próprio — são efeito colateral de abrir/fechar
comanda, para que a mesa nunca divirja da realidade do salão.

```mermaid
stateDiagram-v2
    [*] --> LIVRE
    LIVRE --> RESERVADA: reservar
    RESERVADA --> LIVRE: cancelar a reserva
    LIVRE --> OCUPADA: ocupar (abertura de comanda)
    RESERVADA --> OCUPADA: ocupar (abertura de comanda)
    OCUPADA --> LIVRE: liberar (fechamento de comanda)
    LIVRE --> INTERDITADA: interditar
    RESERVADA --> INTERDITADA: interditar
    INTERDITADA --> LIVRE: liberar a interdição
```

### 4. Ciclo de vida do item na fila de preparo (`StatusPreparo`)

Uma entrada por item de pedido que exige preparo (`ItemCardapio.exigePreparo=true`), não
por pedido — permite roteamento independente por seção (cozinha/bar).

```mermaid
stateDiagram-v2
    [*] --> AGUARDANDO: Item enviado para preparo
    AGUARDANDO --> EM_PREPARO: Cozinheiro inicia
    EM_PREPARO --> CONCLUIDO: Cozinheiro conclui
    AGUARDANDO --> CANCELADO
    EM_PREPARO --> CANCELADO

    CONCLUIDO --> [*]
    CANCELADO --> [*]
```

---

## Gerenciamento de schema com Liquibase

A infraestrutura utiliza o Liquibase para versão e histórico de schema relacional. O Hibernate executa estritamente com `spring.jpa.hibernate.ddl-auto=validate`.

As 16 migrações YAML localizam-se em `src/main/resources/db/changelog/changes/`:

1. `001-create-categoria-produto.yaml`: Tabela inicial de categorias.
2. `002-create-produto.yaml`: Tabela inicial de produtos.
3. `003-create-usuario.yaml`: Tabela de usuários e credenciais.
4. `004-drop-cardapio-legado.yaml`: Refatoração do modelo relacional.
5. `005-create-categoria-cardapio.yaml`: Categorias do cardápio.
6. `006-create-item-cardapio.yaml`: Itens do cardápio e controle de estoque.
7. `007-create-cliente.yaml`: Tabela de clientes.
8. `008-create-mesa.yaml`: Tabela de mesas e capacidade.
9. `009-create-comanda.yaml`: Tabelas de comandas.
10. `010-create-pedido.yaml`: Cabeçalho do pedido.
11. `011-create-item-pedido.yaml`: Itens lançados no pedido.
12. `012-create-preparo-item.yaml`: Fila de preparo da cozinha.
13. `013-create-sessao-caixa.yaml`: Sessões e turnos de caixa.
14. `014-create-sangria.yaml`: Retiradas de dinheiro (sangria) do caixa durante o turno.
15. `015-create-pagamento.yaml`: Formas e registros de pagamento.
16. `016-indices-de-consulta.yaml`: Índices otimizados de banco de dados.

> **Por que `001` e `002` ainda existem se o tema não usa mais `categoria_produto`/`produto`?**
> Essas duas migrações são o esquema original da Aula 04 e já estão no checkpoint imutável
> `aula-04-jpa-postgresql-liquibase`. Liquibase trata cada changeset como um registro histórico
> append-only — editá-los ou removê-los quebraria o checksum de qualquer banco que já os tenha
> executado. A migração `004-drop-cardapio-legado.yaml` documenta explicitamente essa transição,
> derrubando as duas tabelas do tema antigo antes de `005`/`006` criarem `categoria_cardapio`/
> `item_cardapio` no novo desenho. Este é o mesmo padrão usado em evolução de schema real
> (nunca reescrever uma migração já aplicada — sempre migrar para a frente).

---

## Segurança e controle de acesso

Segurança configurada via Spring Security:

- **Autenticação**: HTTP Basic Authentication com credenciais no cabeçalho `Authorization`.
- **Perfis (Roles)**:
  - `ROLE_ADMIN`: Acesso completo.
  - `ROLE_GARCOM`: Abertura de comanda, pedidos, mesas e clientes.
  - `ROLE_COZINHA`: Acesso e manipulação da fila de preparo.
  - `ROLE_CAIXA`: Turnos de caixa, sangrias e recebimento de pagamentos.
- **Formato de Erro**: Payloads RFC 7807 (`application/problem+json`) para erros 401 Unauthorized e 403 Forbidden.

---

## Exemplo de ciclo de vida HTTP

### Requisição: Lançar Pedido em Comanda

```http
POST /api/comandas/1/pedidos HTTP/1.1
Host: localhost:8080
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{
  "observacao": "Sem pimenta",
  "itens": [
    {
      "itemCardapioId": 5,
      "quantidade": 2
    }
  ]
}
```

### Resposta: HTTP 201 Created

```json
{
  "id": 12,
  "comandaId": 1,
  "status": "ABERTO",
  "observacao": "Sem pimenta",
  "valorTotal": 58.00,
  "dataHoraCriacao": "2026-08-26T13:40:00Z",
  "itens": [
    {
      "id": 24,
      "itemCardapioId": 5,
      "nomeItem": "Hambúrguer Artesanal",
      "quantidade": 2,
      "precoUnitario": 29.00,
      "precoTotal": 58.00
    }
  ]
}
```

---

## Perfis de ambiente

- **`dev`**: Ambiente de desenvolvimento local (`restaurante2026_dev`).
- **`test`**: Ambiente de testes automatizados (`restaurante2026_test`).
- **`prod`**: Ambiente de produção com credenciais injetadas por variáveis do ambiente host.

---

## Decisões de engenharia

1. **`BigDecimal` para Valores Monetários**: Prevenção total de inconsistências de arredondamento IEEE 754 de tipos `float`/`double`.
2. **Fidelidade de Testes no PostgreSQL Real**: Suíte de testes executa contra a mesma engine relacional de produção, garantindo suporte real a tipos, constraints e dialeto SQL.
3. **Erros Padronizados (RFC 7807)**: Todo erro HTTP utiliza o contrato `application/problem+json`, garantindo clareza e previsibilidade nos clientes.
