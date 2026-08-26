# Arquitetura

Este documento descreve, em profundidade, como o **Restaurante 2026** funciona por dentro: a topologia de camadas, o modelo de domínio com entidades ricas, as máquinas de estado para comandas e pedidos, o versionamento de banco de dados via Liquibase, o controle de acesso com Spring Security e as decisões de engenharia por trás de cada escolha.

Endpoints detalhados em [`API.md`](API.md). Especificação do tema do projeto em [`tema-do-projeto.md`](tema-do-projeto.md).

> **TL;DR** — O sistema adota uma arquitetura em camadas estritas baseada nos princípios de Domain-Driven Design (DDD). O banco de dados PostgreSQL é versionado incrementalmente via Liquibase (`ddl-auto=validate`), garantindo previsibilidade entre ambientes (`dev`, `test`, `prod`). As regras de negócio e invariantes são totalmente protegidas pelas entidades Java ricas no domínio.

<br>

## Índice

- [Visão Geral](#visão-geral)
- [Camadas e Pacotes](#camadas-e-pacotes)
- [Modelo de Domínio](#modelo-de-domínio)
- [Máquinas de Estado](#máquinas-de-estado)
- [Persistência e Liquibase](#persistência-e-liquibase)
- [Segurança e Autenticação](#segurança-e-autenticação)
- [Exemplo de Fluxo e Payloads HTTP](#exemplo-de-fluxo-e-payloads-http)
- [Perfis de Ambiente](#perfis-de-ambiente)
- [Decisões de Engenharia](#decisões-de-engenharia)

---

## Visão Geral

O princípio central da aplicação é separar a infraestrutura HTTP e a persistência das **regras de negócio puras**. A API RESTful recebe requisições, valida a forma dos dados por meio de DTOs com Jakarta Validation, delega a orquestração para os serviços da camada de aplicação e executa transações sobre entidades ricas.

```mermaid
flowchart LR
    subgraph Client["Cliente HTTP"]
        C["curl / Postman / Insomnia\nInterface Web / App Mobile"]
    end

    subgraph Server["Servidor Spring Boot 3.4"]
        SEC["Spring Security\n(HTTP Basic)"]
        API["Controllers REST\n+ DTOs"]
        SVC["Services\n(Orquestração & Transações)"]
        DOM["Domain\n(Entidades Ricas & Invariantes)"]
        REPO["Spring Data JPA\n(Repositories)"]
    end

    subgraph DB["Infraestrutura de Banco"]
        PG[(PostgreSQL 16)]
        LB["Liquibase\n(16 Migrações YAML)"]
    end

    C -- "HTTP Basic (Basic Auth)" --> SEC
    SEC --> API
    API --> SVC
    SVC --> DOM
    SVC --> REPO
    REPO -- "SQL (Hibernate / JDBC)" --> PG
    LB -- "ddl-auto=validate" --> PG
```

---

## Camadas e Pacotes

A estrutura de código respeita a seguinte divisão de pacotes dentro de `com.curso.restaurante`:

```text
com.curso.restaurante
├── config/            SecurityConfig, Handlers de Erro 401/403, Bootstrap Admin
├── api/               HealthController + Controllers por módulo (dto de entrada e saída)
├── domain/            Entidades ricas, invariantes de negócio, enums e exceções
├── repository/        Interfaces Spring Data JPA estendendo JpaRepository
└── service/           Serviços de orquestração, controle transacional e regras cross-aggregate
```

```mermaid
flowchart TD
    subgraph api ["Camada API / Presentation"]
        Controller["Controllers REST\n(@RestController)"]
        DTO["DTOs (Request / Response)\nValidation (@Valid)"]
    end

    subgraph service ["Camada Service / Application"]
        Service["Services (@Service)\n@Transactional"]
    end

    subgraph domain ["Camada Domain / Business"]
        Entity["Entidades Ricas (Java puro + JPA)\nRegras & Invariantes"]
        Enum["Enums & Máquinas de Estado"]
    end

    subgraph repository ["Camada Repository / Infrastructure"]
        Repo["Spring Data JPA (@Repository)"]
    end

    Controller --> DTO
    Controller --> Service
    Service --> Entity
    Service --> Repo
    Repo --> DB[(PostgreSQL)]
```

### Matriz de Responsabilidades por Camada

| Camada | Responsabilidade Principal | O que NÃO faz |
| :--- | :--- | :--- |
| **`api`** | Tradução HTTP ↔ DTO, serialização JSON, validação de contrato com `@Valid` | Não contém lógica de negócio nem SQL direto |
| **`service`** | Orquestração de casos de uso, controle transacional (`@Transactional`), coordenação entre agregados | Não acessa o banco sem passar pelo Repository |
| **`domain`** | Representação rica dos conceitos de negócio, validação de invariantes, cálculos com `BigDecimal` | Não depende de frameworks web ou Spring Security |
| **`repository`** | Interfaces de acesso ao PostgreSQL via Spring Data JPA e consultas JPQL otimizadas | Não toma decisões de regra de negócio |

---

## Modelo de Domínio

O modelo relacional do **Restaurante 2026** é composto por 12 tabelas principais gerenciadas pelo Liquibase:

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

## Máquinas de Estado

Para garantir a consistência das operações do restaurante, o ciclo de vida de **Pedidos**, **Comandas** e **Sessões de Caixa** é regido por máquinas de estado explícitas. Transições inválidas disparam a exceção `TransicaoDeStatusInvalidaException` (HTTP 409 Conflict).

### 1. Ciclo de Vida do Pedido (`StatusPedido`)

```mermaid
stateDiagram-v2
    [*] --> RECEBIDO: Pedido criado na comanda
    RECEBIDO --> EM_PREPARO: Cozinha inicia preparo
    EM_PREPARO --> PRONTO: Cozinha finaliza prato
    PRONTO --> ENTREGUE: Garçom entrega na mesa
    
    RECEBIDO --> CANCELADO: Cancelamento com justificativa
    EM_PREPARO --> CANCELADO: Cancelamento excepcional
    
    ENTREGUE --> [*]
    CANCELADO --> [*]
```

### 2. Ciclo de Vida da Comanda (`StatusComanda`)

```mermaid
stateDiagram-v2
    [*] --> ABERTA: Comanda vinculada à mesa/cliente
    ABERTA --> CONTA_SOLICITADA: Cliente solicita o fechamento
    CONTA_SOLICITADA --> PAGA: Pagamento registrado no caixa
    PAGA --> FECHADA: Comanda encerrada e mesa liberada
    
    FECHADA --> [*]
```

---

## Persistência e Liquibase

O banco de dados relacional (PostgreSQL) não é manipulado pelo Hibernate diretamente. A propriedade `spring.jpa.hibernate.ddl-auto` é mantida estritamente como **`validate`**.

O **Liquibase** administra 16 migrações incrementais em YAML no diretório `src/main/resources/db/changelog/changes/`:

1. `001-create-categoria-produto.yaml`: Tabela inicial de grupos/categorias.
2. `002-create-produto.yaml`: Tabela inicial de produtos.
3. `003-create-usuario.yaml`: Tabela de usuários e credenciais.
4. `004-drop-cardapio-legado.yaml`: Refatoração do esquema legado.
5. `005-create-categoria-cardapio.yaml`: Categorias do cardápio do restaurante.
6. `006-create-item-cardapio.yaml`: Itens do cardápio com preços e estoque.
7. `007-create-cliente.yaml`: Cadastro de clientes.
8. `008-create-mesa.yaml`: Cadastro de mesas e capacidade.
9. `009-create-comanda.yaml`: Tabelas de comandas abertas/fechadas.
10. `010-create-pedido.yaml`: Cabeçalho do pedido.
11. `011-create-item-pedido.yaml`: Itens inclusos no pedido.
12. `012-create-preparo-item.yaml`: Fila de preparo da cozinha.
13. `013-create-sessao-caixa.yaml`: Sessões e turnos de caixa.
14. `014-create-sangria.yaml`: Movimentações de sangria e suprimento.
15. `015-create-pagamento.yaml`: Formas e registros de pagamento.
16. `016-indices-de-consulta.yaml`: Índices otimizados para alta concorrência.

---

## Segurança e Autenticação

A segurança é fornecida pelo **Spring Security** configurado em `SecurityConfig`:

- **Mecanismo**: HTTP Basic Authentication com credenciais enviadas no cabeçalho `Authorization`.
- **Perfis de Acesso (Roles)**:
  - `ROLE_ADMIN`: Acesso irrestrito a todos os endpoints, cadastros, usuários e gestão financeira de caixa.
  - `ROLE_USUARIO`: Acesso a operações operacionais (abrir comanda, lançar pedido, atualizar fila de cozinha).
- **Tratamento de Erros de Segurança**:
  - `ProblemDetailAuthenticationEntryPoint`: Retorna JSON padronizado RFC 7807 para falhas 401 Unauthorized.
  - `ProblemDetailAccessDeniedHandler`: Retorna JSON padronizado RFC 7807 para falhas 403 Forbidden.

---

## Exemplo de Fluxo e Payloads HTTP

### 1. Criar um Pedido em uma Comanda Aberta

**Requisição**:
```http
POST /api/comandas/1/pedidos HTTP/1.1
Host: localhost:8080
Authorization: Basic YWRtaW46YWRtaW4xMjM=
Content-Type: application/json

{
  "observacao": "Sem cebola no hambúrguer",
  "itens": [
    {
      "itemCardapioId": 5,
      "quantidade": 2
    }
  ]
}
```

**Resposta HTTP 201 Created**:
```json
{
  "id": 12,
  "comandaId": 1,
  "status": "RECEBIDO",
  "observacao": "Sem cebola no hambúrguer",
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

## Perfis de Ambiente

A aplicação aceita três perfis Spring pré-configurados:

| Perfil | Finalidade | Banco de Dados |
| :--- | :--- | :--- |
| **`dev`** | Desenvolvimento local | PostgreSQL (`restaurante2026_dev`) |
| **`test`** | Suíte de testes automatizados | PostgreSQL (`restaurante2026_test`) |
| **`prod`** | Ambiente de produção | PostgreSQL configurado por variáveis de ambiente |

---

## Decisões de Engenharia

1. **Uso de `BigDecimal` em Vez de `Double`**:
   - Moedas e quantidades monetárias no Java nunca devem utilizar tipos primitivos de ponto flutuante (`float`/`double`) para evitar imprecisões de arredondamento IEEE 754.

2. **Rejeição ao Banco H2 em Testes**:
   - Os testes rodam contra o PostgreSQL real no perfil `test`. Isso elimina divergências de dialeto SQL, comportamento de constraints e tipos de dados específicos do SGBD de produção.

3. **Arquitetura Baseada em Contratos (RFC 7807)**:
   - Exceções e erros da API utilizam a especificação Spring `ProblemDetail`, retornando payloads uniformes para erros 400, 401, 403, 404, 409 e 500.
