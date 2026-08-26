# Especificação de API REST — Restaurante 2026

Documentação completa dos endpoints, contratos de requisição/resposta, parâmetros de paginação e códigos de status HTTP da API de Gestão de Restaurantes.

Para detalhes de modelagem e arquitetura em camadas, consulte [`ARQUITETURA.md`](ARQUITETURA.md).

---

## 1. Visão Geral

- **Base URL Local**: `http://localhost:8080`
- **Formato dos Dados**: `application/json` (UTF-8)
- **Formato de Datas**: Padrão ISO-8601 (`YYYY-MM-DD` para datas e `YYYY-MM-DDTHH:mm:ssZ` para data/hora)
- **Segurança**: Todos os endpoints exigem autenticação via HTTP Basic Header (`Authorization: Basic <credentials>`), **com exceção** do endpoint público `/api/health`.

---

## 2. Autenticação e Perfis de Acesso

A autenticação é baseada no padrão **HTTP Basic Authentication**. A credencial deve ser fornecida no cabeçalho de cada requisição.

Exemplo de chamada via `curl`:
```bash
curl -u admin:admin123 http://localhost:8080/api/auth/me
```

Exemplo via `PowerShell`:
```powershell
Invoke-RestMethod http://localhost:8080/api/auth/me -Credential admin -Authentication Basic
```

### Matriz de Permissões por Papel (Roles)

| Papel (Role) | Permissões no Sistema |
| :--- | :--- |
| `ROLE_ADMIN` | Acesso irrestrito: cadastros, gerenciamento de usuários, configuração, relatórios e caixa. |
| `ROLE_GARCOM` | Abertura/fechamento de comandas, lançamento e consulta de pedidos, gestão de ocupação de mesas e clientes. |
| `ROLE_COZINHA` | Visualização da fila de preparo em tempo real, alteração de status do prato (*EM_PREPARO* / *CONCLUIDO*). |
| `ROLE_CAIXA` | Abertura e fechamento de turnos de caixa, registro de sangria/suprimento, recebimento de pagamentos e quitação de comandas. |

---

## 3. Padrão de Paginação

Todos os endpoints de consulta que retornam coleções de dados aceitam os parâmetros query `page` (0-based) e `size` (padrão 20), retornando a estrutura paginada:

```json
{
  "conteudo": [
    {
      "id": 1,
      "nome": "Item do Cardápio"
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

## 4. Tratamento Padrão de Erros (RFC 7807)

Todas as exceções e erros da API retornam payloads estruturados no formato `application/problem+json`:

### 4.1 Erro de Regra de Negócio (HTTP 422 / HTTP 409)
```json
{
  "status": 409,
  "title": "Conflito de estado",
  "detail": "Transição de status inválida: pedido já foi entregue ao cliente",
  "instance": "/api/pedidos/12/cancelar"
}
```

### 4.2 Erro de Validação de Campos (HTTP 400 Bad Request)
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

---

## 5. Endpoints do Sistema

### 5.1 Saúde e Autenticação

| Método | Endpoint | Função | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | Verifica a saúde da aplicação | Público |
| `GET` | `/api/auth/me` | Retorna o perfil do usuário autenticado | Autenticado |

### 5.2 Cardápio

| Método | Endpoint | Função | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/cardapio/categorias` | Lista categorias de itens | `GARCOM` / `ADMIN` |
| `POST` | `/api/cardapio/categorias` | Cadastra nova categoria | `ADMIN` |
| `GET` | `/api/cardapio/itens` | Lista itens do cardápio paginados | `GARCOM` / `ADMIN` |
| `POST` | `/api/cardapio/itens` | Cadastra novo item no cardápio | `ADMIN` |
| `PUT` | `/api/cardapio/itens/{id}` | Atualiza preço ou dados do item | `ADMIN` |
| `POST` | `/api/cardapio/itens/{id}/estoque` | Ajusta o saldo de estoque | `ADMIN` |

### 5.3 Mesas e Clientes

| Método | Endpoint | Função | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/mesas` | Lista mesas e status de ocupação | `GARCOM` / `ADMIN` |
| `POST` | `/api/mesas` | Cadastra nova mesa | `ADMIN` |
| `POST` | `/api/mesas/{id}/reservar` | Reserva uma mesa | `GARCOM` / `ADMIN` |
| `GET` | `/api/clientes` | Pesquisa clientes cadastrados | `GARCOM` / `CAIXA` |
| `POST` | `/api/clientes` | Cadastra novo cliente | `GARCOM` / `CAIXA` |

### 5.4 Comandas e Pedidos

| Método | Endpoint | Função | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/comandas/abrir` | Abre comanda para mesa ou cliente | `GARCOM` |
| `GET` | `/api/comandas/{id}` | Detalhes do consumo e itens da comanda | `GARCOM` / `CAIXA` |
| `POST` | `/api/comandas/{id}/pedidos` | Lança novo pedido na comanda | `GARCOM` |
| `POST` | `/api/pedidos/{id}/enviar-para-preparo` | Envia os itens do pedido para a cozinha | `GARCOM` |
| `POST` | `/api/pedidos/{id}/marcar-entregue` | Confirma a entrega na mesa | `GARCOM` |
| `POST` | `/api/comandas/{id}/fechar` | Solicita o fechamento da conta | `GARCOM` / `CAIXA` |

### 5.5 Cozinha (Fila de Preparo)

| Método | Endpoint | Função | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/cozinha/fila` | Lista pedidos aguardando na cozinha | `COZINHA` |
| `POST` | `/api/cozinha/fila/{id}/iniciar` | Inicia o preparo de um prato | `COZINHA` |
| `POST` | `/api/cozinha/fila/{id}/concluir` | Finaliza o preparo de um prato | `COZINHA` |

### 5.6 Caixa e Financeiro

| Método | Endpoint | Função | Papel Mínimo |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/caixa/sessoes/abrir` | Abre novo turno de caixa | `CAIXA` / `ADMIN` |
| `POST` | `/api/caixa/sangria` | Registra retirada/sangria de caixa | `CAIXA` / `ADMIN` |
| `POST` | `/api/caixa/pagamentos` | Recebe e quita o valor da comanda | `CAIXA` |
| `POST` | `/api/caixa/sessoes/{id}/fechar` | Encerra e confere o turno de caixa | `CAIXA` / `ADMIN` |
