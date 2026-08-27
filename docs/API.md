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
| `ROLE_CAIXA` | Abertura e fechamento de turnos de caixa, sangrias e quitação de comandas. |

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

Tabela completa, gerada a partir dos `@RequestMapping` reais dos controllers e da matriz de
autorização em `SecurityConfig`. "Papel Mínimo" lista os papéis que passam pelo filtro de
segurança; "Autenticado" significa que qualquer usuário logado, de qualquer papel, tem acesso.

### 1. Saúde e Autenticação

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | Status de execução da API | Público |
| `GET` | `/api/auth/me` | Dados do usuário autenticado | Autenticado |

### 2. Usuários

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/usuarios` | Cadastra novo usuário | `ADMIN` |
| `GET` | `/api/usuarios` | Lista usuários paginados | `ADMIN` |
| `GET` | `/api/usuarios/{id}` | Consulta um usuário | `ADMIN` |
| `PUT` | `/api/usuarios/{id}` | Atualiza dados de um usuário | `ADMIN` |
| `PATCH` | `/api/usuarios/{id}/senha` | Altera a própria senha | Autenticado |
| `POST` | `/api/usuarios/{id}/ativar` | Reativa um usuário | `ADMIN` |
| `POST` | `/api/usuarios/{id}/inativar` | Inativa um usuário | `ADMIN` |

### 3. Cardápio

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/categorias-cardapio` | Cadastra categoria de cardápio | `ADMIN` |
| `GET` | `/api/categorias-cardapio` | Lista categorias paginadas | Autenticado |
| `GET` | `/api/categorias-cardapio/{id}` | Consulta uma categoria | Autenticado |
| `PUT` | `/api/categorias-cardapio/{id}` | Atualiza uma categoria | `ADMIN` |
| `POST` | `/api/categorias-cardapio/{id}/ativar` | Reativa uma categoria | `ADMIN` |
| `POST` | `/api/categorias-cardapio/{id}/inativar` | Inativa uma categoria | `ADMIN` |
| `POST` | `/api/itens-cardapio` | Cadastra item do cardápio | `ADMIN` |
| `GET` | `/api/itens-cardapio` | Lista itens paginados | Autenticado |
| `GET` | `/api/itens-cardapio/{id}` | Consulta um item | Autenticado |
| `PUT` | `/api/itens-cardapio/{id}` | Atualiza um item | `ADMIN` |
| `POST` | `/api/itens-cardapio/{id}/entradas-estoque` | Registra entrada de estoque | `ADMIN` |
| `POST` | `/api/itens-cardapio/{id}/saidas-estoque` | Registra saída de estoque | `ADMIN` |
| `POST` | `/api/itens-cardapio/{id}/ativar` | Reativa um item | `ADMIN` |
| `POST` | `/api/itens-cardapio/{id}/inativar` | Inativa um item | `ADMIN` |

### 4. Clientes

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/clientes` | Cadastra novo cliente | `GARCOM` / `CAIXA` / `ADMIN` |
| `GET` | `/api/clientes` | Pesquisa clientes paginados | Autenticado |
| `GET` | `/api/clientes/{id}` | Consulta um cliente | Autenticado |
| `PUT` | `/api/clientes/{id}` | Atualiza um cliente | `GARCOM` / `CAIXA` / `ADMIN` |
| `GET` | `/api/clientes/{id}/comandas` | Histórico de comandas do cliente | Autenticado |
| `POST` | `/api/clientes/{id}/ativar` | Reativa um cliente | `ADMIN` |
| `POST` | `/api/clientes/{id}/inativar` | Inativa um cliente | `ADMIN` |

### 5. Mesas

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/mesas` | Cadastra nova mesa | `ADMIN` |
| `GET` | `/api/mesas` | Consulta ocupação de mesas | Autenticado |
| `GET` | `/api/mesas/{id}` | Consulta uma mesa | Autenticado |
| `PUT` | `/api/mesas/{id}` | Atualiza uma mesa | `ADMIN` |
| `POST` | `/api/mesas/{id}/reservar` | Reserva uma mesa livre | `GARCOM` / `ADMIN` |
| `POST` | `/api/mesas/{id}/cancelar-reserva` | Cancela reserva de uma mesa | `GARCOM` / `ADMIN` |
| `POST` | `/api/mesas/{id}/interditar` | Interdita uma mesa | `ADMIN` |
| `POST` | `/api/mesas/{id}/liberar-interdicao` | Libera interdição de uma mesa | `ADMIN` |

### 6. Comandas e Pedidos

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/comandas` | Abre comanda para mesa ou cliente | `GARCOM` / `CAIXA` / `ADMIN` |
| `GET` | `/api/comandas` | Lista comandas paginadas | Autenticado |
| `GET` | `/api/comandas/{id}` | Consulta uma comanda | Autenticado |
| `GET` | `/api/comandas/{id}/conta` | Consulta subtotal, taxa e saldo devedor | Autenticado |
| `POST` | `/api/comandas/{id}/fechar` | Solicita fechamento de conta | `GARCOM` / `CAIXA` / `ADMIN` |
| `POST` | `/api/comandas/{id}/reabrir` | Reabre uma comanda fechada | `CAIXA` / `ADMIN` |
| `POST` | `/api/comandas/{id}/cancelar` | Cancela uma comanda aberta | `ADMIN` |
| `POST` | `/api/comandas/{id}/transferir-mesa` | Transfere a comanda para outra mesa | `GARCOM` / `ADMIN` |
| `POST` | `/api/comandas/{comandaId}/pedidos` | Lança pedido em comanda aberta | `GARCOM` / `ADMIN` |
| `GET` | `/api/pedidos` | Lista pedidos paginados | Autenticado |
| `GET` | `/api/pedidos/{id}` | Consulta um pedido | Autenticado |
| `POST` | `/api/pedidos/{id}/itens` | Adiciona item ao pedido | `GARCOM` / `ADMIN` |
| `DELETE` | `/api/pedidos/{id}/itens/{itemId}` | Remove item do pedido | `GARCOM` / `ADMIN` |
| `POST` | `/api/pedidos/{id}/enviar-para-preparo` | Encaminha itens para a cozinha | `GARCOM` / `ADMIN` |
| `POST` | `/api/pedidos/{id}/marcar-pronto` | Marca pedido como pronto | `COZINHA` / `ADMIN` |
| `POST` | `/api/pedidos/{id}/marcar-entregue` | Confirma entrega do pedido na mesa | `GARCOM` / `ADMIN` |
| `POST` | `/api/pedidos/{id}/cancelar` | Cancela um pedido (antes de `ENTREGUE`) | `GARCOM` / `CAIXA` / `ADMIN` |

### 7. Cozinha

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/cozinha/fila` | Lista fila de preparo, ordenada por prioridade | `COZINHA` / `ADMIN` |
| `GET` | `/api/cozinha/fila/{id}` | Consulta um item da fila | `COZINHA` / `ADMIN` |
| `POST` | `/api/cozinha/fila/{id}/iniciar` | Marca início do preparo do prato | `COZINHA` / `ADMIN` |
| `POST` | `/api/cozinha/fila/{id}/concluir` | Marca conclusão do preparo do prato | `COZINHA` / `ADMIN` |
| `POST` | `/api/cozinha/fila/{id}/cancelar` | Cancela um item da fila | `COZINHA` / `ADMIN` |
| `POST` | `/api/cozinha/fila/{id}/alterar-prioridade` | Altera a prioridade de um item | `COZINHA` / `ADMIN` |

### 8. Caixa e Financeiro

| Método | Caminho | Descrição | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/caixa/sessoes` | Abre turno de caixa | `CAIXA` / `ADMIN` |
| `GET` | `/api/caixa/sessoes` | Lista sessões de caixa paginadas | `CAIXA` / `ADMIN` |
| `GET` | `/api/caixa/sessoes/aberta` | Consulta a sessão atualmente aberta | `CAIXA` / `ADMIN` |
| `GET` | `/api/caixa/sessoes/{id}` | Consulta uma sessão | `CAIXA` / `ADMIN` |
| `POST` | `/api/caixa/sessoes/{id}/fechar` | Encerra e confere o turno de caixa | `CAIXA` / `ADMIN` |
| `POST` | `/api/caixa/sessoes/{id}/sangrias` | Registra sangria na sessão aberta | `CAIXA` / `ADMIN` |
| `GET` | `/api/caixa/sessoes/{id}/sangrias` | Lista sangrias de uma sessão | `CAIXA` / `ADMIN` |
| `GET` | `/api/caixa/sessoes/{id}/pagamentos` | Lista pagamentos de uma sessão | `CAIXA` / `ADMIN` |
| `POST` | `/api/comandas/{comandaId}/pagamentos` | Registra pagamento e quita a comanda | `CAIXA` / `ADMIN` |
| `GET` | `/api/comandas/{comandaId}/pagamentos` | Lista pagamentos de uma comanda | `CAIXA` / `ADMIN` |
