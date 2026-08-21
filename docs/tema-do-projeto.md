# Tema do projeto individual

Projeto tematico desenvolvido a partir do repositorio de referencia [`suporteos2026`](https://github.com/jeffersonarpasserini/suporteos2026), seguindo a estrutura pedida nas Aulas 00 a 04.

## Identificacao

- Nome: `restaurante2026`
- Tema: gestao de pedidos de restaurante
- Objetivo: cadastrar os itens do cardapio e organiza-los por categoria

## Entidade de classificacao

- Nome no singular: `CategoriaProduto`
- Nome no plural: categorias de produto
- Descricao: classificacao utilizada para organizar os itens do cardapio
- Exemplos: Entradas, Pratos Principais, Bebidas, Sobremesas
- Status: ativa ou inativa

## Entidade principal

- Nome no singular: `Produto`
- Nome no plural: produtos
- Codigo unico: codigo do item no cardapio
- Descricao: nome do prato ou bebida
- Medida quantitativa: saldo disponivel em estoque
- Valor monetario: valor unitario de venda
- Valor calculado: saldo em estoque multiplicado pelo valor unitario
- Data relevante: data de cadastro no cardapio
- Status: ativo ou inativo

## Relacionamento

- Uma categoria de produto pode classificar varios produtos.
- Cada produto pertence a uma categoria de produto.

## Exemplos

| Categoria | Codigo | Produto | Saldo | Valor unitario |
|---|---|---|---:|---:|
| Entradas | `PRD-0001` | Bruschetta | 12 | 22,00 |
| Pratos Principais | `PRD-0002` | Risoto de Cogumelos | 8 | 48,50 |
| Bebidas | `PRD-0003` | Suco Natural | 30 | 9,00 |
| Sobremesas | `PRD-0004` | Petit Gateau | 10 | 19,90 |

## Estado atual do projeto

O repositorio esta no ponto de quebra da **Aula 04**:

- `CategoriaProduto` e `Produto` implementados em Java puro (Aula 03), com regras de negocio (calculo de valor em estoque, entrada/saida de estoque, associacao bidirecional, validacoes) cobertas por testes unitarios.
- Persistencia com Spring Data JPA, PostgreSQL e Liquibase (Aula 04): o Liquibase cria e versiona o esquema (`categoria_produto`, `produto`), o Hibernate apenas valida o mapeamento (`ddl-auto=validate`).
- Profiles `dev` e `test` configurados via `.env` local, sem H2 e sem valores fixos no codigo-fonte.

O que ainda nao existe: controllers REST para `CategoriaProduto`/`Produto` (nao fazem parte das Aulas 00-04) e o modulo de pedidos propriamente dito, que depende de aulas futuras do curso.
