# Especificação de API REST — Restaurante 2026

Documentação de referência técnica dos endpoints, contratos de transferência (DTOs), paginação, permissões de acesso e padronização de erros da API.

Para detalhes de arquitetura em camadas e modelo relacional, consulte [ARQUITETURA.md](ARQUITETURA.md).

---

## Índice

- [Base e protocolo](#base-e-protocolo)
- [Autenticação e permissões](#autenticação-e-permissões)
- [Contrato de paginação](#contrato-de-paginação)
- [Padronização de erros (RFC 7807)](#padronização-de-erros-rfc-7807)
- [Mapeamento de endpoints](#mapeamento-de-endpoints)

---

## Base e protocolo

- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json` (UTF-8)
- **Formato de Datas**: ISO-8601 (`YYYY-MM-DD` / `YYYY-MM-DDTHH:mm:ssZ`)
- **Segurança**: HTTP Basic Header (`Authorization: Basic <credentials>`), exceto `/api/health`.

---

## Autenticação e permissões

A API exige credenciais HTTP Basic enviadas no cabeçalho HTTP de cada requisição.

```bash
curl -u admin:admin123 http://localhost:8080/api/auth/me
```

### Matriz de papéis (Roles)

| Papel | Permissões no Sistema |
| :--- | :--- |
| `ROLE_ADMIN` | Acesso irrestrito a cadastros, usuários, relatórios e controle financeiro de caixa. |
| `ROLE_GARCOM` | Abertura/fechamento de comandas, lançamento de pedidos, reservas de mesa e clientes. |
| `ROLE_COZINHA` | Leitura e atualização da fila de preparo da cozinha. |
| `ROLE_CAIXA` | Abertura e fechamento de turnos de caixa, sangrias, suprimentos e quitação de comandas. |

---

## Contrato de paginação

Endpoints de listagem aceitam os parâmetros query `page` (0-based) e `size` (padrão 20):

```json
{
  "conteudo": [
    {
      "id": 1,
      "descricao": "Item do Cardápio"
    }
  ],
  "pagina": 0,
  "tamanho": 20,
  "totalElementos": 42,
  "totalPaginas": 3,
  "primeira": true,
  "ultima": false
}
```

---

## Padronização de erros (RFC 7807)

Todas as falhas da API retornam respostas padronizadas no formato `application/problem+json`:

### 1. Erro de Regra de Negócio ou Estado (HTTP 409 / 422)

```json
{
  "status": 409,
  "title": "Conflito de estado",
  "detail": "Transição de status inválida: o pedido já foi entregue",
  "instance": "/api/pedidos/12/cancelar"
}
```

### 2. Erro de Validação de Campos (HTTP 400 Bad Request)

```json
{
  "status": 400,
  "title": "Requisição inválida",
  "detail": "Um ou mais campos contêm erros de validação",
  "campos": [
    {
      "campo": "precoVenda",
      "mensagem": "deve ser maior que zero"
    }
  ]
}
```

| Código HTTP | Causa |
| :--- | :--- |
| **400 Bad Request** | Erro de sintaxe JSON ou falha de Bean Validation (`@Valid`) |
| **401 Unauthorized** | Credenciais ausentes ou incorretas |
| **403 Forbidden** | Usuário autenticado com papel insuficiente para a operação |
| **404 Not Found** | Identificador de recurso inexistente |
| **409 Conflict** | Conflito de estado ou violação de unicidade no banco |
| **422 Unprocessable Entity** | Regra de negócio violada durante o processamento |

---

## Mapeamento de endpoints

### 1. Saúde e Autenticação

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | Status de execução da API | Público |
| `GET` | `/api/auth/me` | Dados do usuário autenticado | Autenticado |

### 2. Cardápio

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/cardapio/categorias` | Lista categorias de itens | `GARCOM` / `ADMIN` |
| `POST` | `/api/cardapio/categorias` | Cadastra nova categoria | `ADMIN` |
| `GET` | `/api/cardapio/itens` | Lista itens do cardápio paginados | `GARCOM` / `ADMIN` |
| `POST` | `/api/cardapio/itens` | Cadastra novo item no cardápio | `ADMIN` |
| `PUT` | `/api/cardapio/itens/{id}` | Atualiza item do cardápio | `ADMIN` |
| `POST` | `/api/cardapio/itens/{id}/estoque` | Movimenta saldo de estoque | `ADMIN` |

### 3. Mesas e Clientes

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/mesas` | Consulta ocupação de mesas | `GARCOM` / `ADMIN` |
| `POST` | `/api/mesas` | Cadastra nova mesa | `ADMIN` |
| `POST` | `/api/mesas/{id}/reservar` | Reserva uma mesa | `GARCOM` / `ADMIN` |
| `GET` | `/api/clientes` | Pesquisa clientes cadastrados | `GARCOM` / `CAIXA` |
| `POST` | `/api/clientes` | Cadastra novo cliente | `GARCOM` / `CAIXA` |

### 4. Comandas e Pedidos

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/comandas/abrir` | Abre comanda para mesa ou cliente | `GARCOM` |
| `GET` | `/api/comandas/{id}` | Consulta consumo e conta da comanda | `GARCOM` / `CAIXA` |
| `POST` | `/api/comandas/{id}/pedidos` | Lança pedido em comanda aberta | `GARCOM` |
| `POST` | `/api/pedidos/{id}/enviar-para-preparo` | Encaminha itens para a cozinha | `GARCOM` |
| `POST` | `/api/pedidos/{id}/marcar-entregue` | Confirma entrega do pedido na mesa | `GARCOM` |
| `POST` | `/api/comandas/{id}/fechar` | Solicita fechamento de conta | `GARCOM` / `CAIXA` |

### 5. Cozinha

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/cozinha/fila` | Lista pedidos pendentes na cozinha | `COZINHA` |
| `POST` | `/api/cozinha/fila/{id}/iniciar` | Marca início do preparo do prato | `COZINHA` |
| `POST` | `/api/cozinha/fila/{id}/concluir` | Marca conclusão do preparo do prato | `COZINHA` |

### 6. Caixa e Financeiro

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/caixa/sessoes/abrir` | Abre turno de caixa | `CAIXA` / `ADMIN` |
| `POST` | `/api/caixa/sangria` | Registra sangria ou suprimento de caixa | `CAIXA` / `ADMIN` |
| `POST` | `/api/caixa/pagamentos` | Registra pagamento e quita comanda | `CAIXA` |
| `POST` | `/api/caixa/sessoes/{id}/fechar` | Encerra e confere o turno de caixa | `CAIXA` / `ADMIN` |
