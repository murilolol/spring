# Spec: restaurante2026 - Gestao de Pedidos de Restaurante

## 1. Problem Statement & Objectives

- **Context**: Projeto tematico da disciplina de Desenvolvimento de Sistemas, construido seguindo a estrutura pedagogica das Aulas 00 a 04 do repositorio de referencia [`suporteos2026`](https://github.com/jeffersonarpasserini/suporteos2026) (Prof. Jefferson Passerini).
- **Goals**:
  - Estrutura de repositorio identica ao esperado nas Aulas 00-02 (README, `.editorconfig`, `.gitignore`, projeto Spring Boot minimo, `GET /api/health`).
  - Dominio `CategoriaProduto`/`Produto` em Java puro, sem dependencia de framework de persistencia (Aula 03).
  - Persistencia via Spring Data JPA, PostgreSQL e Liquibase, com Hibernate em modo `validate` (Aula 04).
  - Documentacao (`README.md`, `docs/ARQUITETURA.md`, `docs/tema-do-projeto.md`) com diagramas Mermaid descrevendo o estado atual.
- **Non-Goals**:
  - Controllers REST de `CategoriaProduto`/`Produto` — nao fazem parte das Aulas 00-04.
  - Modulo de pedidos, mesas, cozinha ou caixa — depende de aulas futuras nao publicadas no repositorio de referencia.
  - Frontend web — fora do escopo do curso `suporteos2026`.

## 2. Technical Design & Architecture

- **Backend**: Spring Boot 4.0.7, Java 21, empacotamento Jar, servidor Tomcat embutido.
- **Dependencias**: `spring-boot-starter-webmvc`, `spring-boot-starter-validation`, `spring-boot-starter-data-jpa`, `spring-boot-starter-liquibase`, `postgresql` (runtime), `spring-boot-devtools` (runtime/opcional); starters de teste equivalentes mais `spring-boot-starter-data-jpa-test`.
- **Pacote raiz**: `com.curso.restaurante`.
  - `api.HealthController` — `GET /api/health`.
  - `domain.CategoriaProduto`, `domain.Produto`, `domain.Status` — entidades JPA com regras de negocio proprias.
- **Persistencia**: Liquibase versiona o esquema (`categoria_produto`, `produto`, 8 changesets); Hibernate roda em `ddl-auto=validate`. Profiles `dev`/`test`/`prod` sem H2.
- Diagramas de arquitetura completos em [`docs/ARQUITETURA.md`](../docs/ARQUITETURA.md).

## 3. Step-by-Step Implementation Plan

1. [x] **Fase 1: Realinhamento ao ponto de quebra da Aula 02**
   - Projeto Maven na raiz do repositorio, `groupId com.curso`, `artifactId restaurante2026`, pacote `com.curso.restaurante`.
   - `pom.xml` reduzido as dependencias da Aula 02; endpoint `GET /api/health`.
2. [x] **Fase 2: Modelagem de dominio (Aula 03)**
   - `CategoriaProduto` e `Produto` em Java puro, com validacao de invariantes no construtor e comportamento explicito (`receberEstoque`, `retirarEstoque`, `calcularValorEstoque`, `ativar`/`inativar`).
   - Testes unitarios cobrindo regras e casos de erro.
3. [x] **Fase 3: Persistencia (Aula 04)**
   - Anotacoes JPA em `CategoriaProduto`/`Produto`, mapeamento `@OneToMany`/`@ManyToOne`.
   - Changelogs Liquibase (`categoria_produto`, `produto`, unicidade, FK, checks de saldo/valor/status).
   - Profiles `dev`/`test`/`prod` lendo credenciais de `.env` (nao versionado) ou variaveis de ambiente.
   - Testes de persistencia com `EntityManager`/`JdbcTemplate` contra PostgreSQL real.
4. [x] **Fase 4: Documentacao**
   - `README.md`, `docs/ARQUITETURA.md` e `docs/tema-do-projeto.md` reescritos com diagramas Mermaid, sem marcas de geracao automatica.

## 4. Acceptance Criteria & Edge Cases

- **Criteria 1**: `./mvnw test` (ou `.\mvnw.cmd test`) termina com `BUILD SUCCESS` com PostgreSQL local disponivel e `.env` preenchido.
- **Criteria 2**: `GET /api/health` retorna `200` com corpo `OK`, nos profiles `dev` e `test`.
- **Criteria 3**: Liquibase aplica os 8 changesets sem erro em um banco vazio; Hibernate valida o mapeamento sem tentar alterar o esquema.
- **Criteria 4**: Banco rejeita codigo de produto duplicado e saldo/valor negativos mesmo fora da aplicacao Java (constraints `UNIQUE`/`CHECK`).
- **Criteria 5**: Repositorio nao contem WebSocket, CRUD de pedidos/mesas/usuarios ou frontend — fora do escopo das Aulas 00-04.
- **Edge Case 1**: Nenhuma credencial, `.env` real ou chave privada versionada (`.gitignore` cobre `.env*`).
