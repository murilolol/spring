<h1 align="center">RESTAURANTE 2026</h1>

<p align="center"><em>API RESTful de gestão completa de restaurantes desenvolvida no curso de Sistemas de Informação da <strong>UniFEF</strong>.</em></p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-4.0.7-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 4.0.7" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql&logoColor=white" alt="PostgreSQL 16" />
  <img src="https://img.shields.io/badge/Liquibase-Schema_Migrations-2E8B57?style=flat-square&logo=liquibase&logoColor=white" alt="Liquibase" />
  <img src="https://img.shields.io/badge/Spring_Security-HTTP_Basic-6DB33F?style=flat-square&logo=springsecurity&logoColor=white" alt="Spring Security" />
  <img src="https://img.shields.io/badge/Maven-Wrapper-C71A36?style=flat-square&logo=apachemaven&logoColor=white" alt="Maven Wrapper" />
  <img src="https://img.shields.io/badge/JUnit_5-354_tests_passed-25A162?style=flat-square&logo=junit5&logoColor=white" alt="JUnit 5" />
</p>

<br>

> **TL;DR** — Sistema backend robusto para automação operacional de estabelecimentos gastronômicos: cardápio, clientes, mesas, comandas, pedidos com máquina de estados, fila de preparo da cozinha e gestão de caixa. Arquitetura em camadas baseada em Domain-Driven Design (DDD) e persistência relacional com migrações auditáveis via Liquibase. Detalhes completos em [docs/ARQUITETURA.md](docs/ARQUITETURA.md) e [docs/API.md](docs/API.md).

<br>

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Engenharia e padrões de projeto](#engenharia-e-padrões-de-projeto)
- [Módulos do sistema](#módulos-do-sistema)
- [Stack tecnológica](#stack-tecnológica)
- [Como executar](#como-executar)
- [Autenticação e segurança](#autenticação-e-segurança)
- [Documentação complementar](#documentação-complementar)
- [Autoria](#autoria)

<br>

## Sobre o projeto

O **Restaurante 2026** é uma solução backend de alta performance construída em Java 21 e Spring Boot para gerenciamento de atendimento em restaurantes, bares e lanchonetes.

A aplicação automatiza o fluxo operacional desde a chegada do cliente e ocupação de mesa até o lançamento de comandas, enfileiramento na cozinha, entrega de pratos e quitação financeira no caixa.

<br>

## Engenharia e padrões de projeto

- **Domain-Driven Design (DDD)**: Entidades ricas que garantem invariantes de negócio e encapsulam regras de cálculo diretamente nos objetos de domínio.
- **Persistência Auditável**: Migrações de banco gerenciadas incrementalmente pelo Liquibase (16 changesets em YAML), executando no Spring Boot com `ddl-auto=validate`.
- **Precisão Financeira Estrita**: Emprego exclusivo de `java.math.BigDecimal` para moedas e quantidades, eliminando inconsistências de arredondamento de ponto flutuante.
- **Máquinas de Estado Finitas**: Ciclo de vida estrito para Pedidos (*ABERTO → EM_PREPARO → PRONTO → ENTREGUE → PAGO*, com cancelamento possível em qualquer etapa até `PRONTO`) e Comandas (*ABERTA → FECHADA → PAGA*, com reabertura de `FECHADA` para `ABERTA` e cancelamento a partir de `ABERTA`).
- **Contrato de Erros RFC 7807**: Tratamento global de exceções produzindo payloads padronizados no formato `application/problem+json`.
- **Fidelidade Tecnológica de Testes**: Cobertura integrada com 354 testes executando sobre PostgreSQL 16 real, descartando mocks de banco em memória.

<br>

## Módulos do sistema

1. **Usuários**: Autenticação, papéis (`ADMIN`, `GARCOM`, `COZINHA`, `CAIXA`) e bootstrap de administrador.
2. **Cardápio**: Categorização, precificação, seções de preparo (Cozinha/Bar) e controle de estoque.
3. **Clientes**: Cadastro e histórico de consumo.
4. **Mesas**: Controle de ocupação do salão (*LIVRE*, *OCUPADA*, *RESERVADA*, *INTERDITADA*).
5. **Comandas**: Vinculação por mesa ou cliente, aglutinação de consumo e fechamento de conta.
6. **Pedidos**: Lançamento de itens e orquestração de transição de status.
7. **Cozinha**: Fila de preparo distribuída por seção com acompanhamento ao vivo.
8. **Caixa**: Turnos de abertura/fechamento, sangrias e pagamentos.

<br>

## Stack tecnológica

| Camada | Tecnologia |
| :--- | :--- |
| **Linguagem** | Java 21 (LTS) |
| **Framework Core** | Spring Boot 4.0.7 (Spring Web MVC, Spring Validation, Spring Data JPA, Spring Security) |
| **Persistência / ORM** | Hibernate 7.x / JPA 3.1 |
| **Banco de Dados** | PostgreSQL 16 |
| **Migração de Schema** | Liquibase |
| **Segurança** | Spring Security (HTTP Basic) |
| **Build & Tooling** | Apache Maven (Maven Wrapper) |
| **Testes** | JUnit 5, MockMvc, Spring Security Test |

<br>

## Como executar

### Pré-requisitos
- Java 21 (JDK 21+)
- PostgreSQL 16+ (local ou via Docker na porta `5432`)
- Git

### 1. Criar o banco de dados no PostgreSQL

Execute em seu cliente PostgreSQL (`psql` ou pgAdmin):

```sql
CREATE ROLE restaurante_app WITH LOGIN PASSWORD 'sua_senha_local';
CREATE DATABASE restaurante2026_dev OWNER restaurante_app;
CREATE DATABASE restaurante2026_test OWNER restaurante_app;
```

### 2. Configurar o arquivo `.env`

Crie uma cópia do modelo `.env.example` na raiz do projeto:

```bash
cp .env.example .env
```

Preencha com a senha definida no PostgreSQL:

```env
DB_DEV_URL=jdbc:postgresql://localhost:5432/restaurante2026_dev
DB_DEV_USERNAME=restaurante_app
DB_DEV_PASSWORD=sua_senha_local

DB_TEST_URL=jdbc:postgresql://localhost:5432/restaurante2026_test
DB_TEST_USERNAME=restaurante_app
DB_TEST_PASSWORD=sua_senha_local

ADMIN_BOOTSTRAP_PASSWORD=admin123
```

### 3. Rodar a aplicação pelo Maven Wrapper

> ⚠️ **O comando precisa da flag de perfil no final.** `application.properties`
> define `spring.profiles.default=none` de propósito, para forçar a escolha
> consciente do ambiente — `.\mvnw.cmd spring-boot:run` **sozinho, sem a
> flag, sempre vai falhar** com `Failed to configure a DataSource: 'url'
> attribute is not specified`. Copie o comando completo abaixo, com a flag.

No Windows (PowerShell), copie o comando inteiro, incluindo a flag no final:
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

No Linux / macOS (Terminal):
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

A API estará pronta em `http://localhost:8080`.

### 4. Executar os testes automatizados

```powershell
.\mvnw.cmd test
```

<br>

## Autenticação e segurança

A API adota **HTTP Basic Authentication**, sessões stateless e senhas com hash BCrypt. Nos
perfis `dev` e `test`, um usuário administrador inicial é registrado automaticamente no
bootstrap (`config/BootstrapUsuarioAdmin`) se ainda não existir nenhum usuário no banco:

- **Usuário**: `admin`
- **Senha padrão**: `admin123` (lida de `ADMIN_BOOTSTRAP_PASSWORD` no `.env`)

Exemplo de chamada com `curl`:
```bash
curl -u admin:admin123 http://localhost:8080/api/auth/me
```

Matriz completa de papéis (`ADMIN`, `GARCOM`, `COZINHA`, `CAIXA`) por endpoint em
[docs/API.md](docs/API.md#autenticação-e-permissões).

<br>

## Documentação complementar

- [docs/ARQUITETURA.md](docs/ARQUITETURA.md) — Diagramas Mermaid de camadas, modelo relacional E-R de 12 tabelas e máquinas de estado.
- [docs/API.md](docs/API.md) — Especificação completa de endpoints, contratos DTO, paginação e respostas HTTP JSON.
- [docs/tema-do-projeto.md](docs/tema-do-projeto.md) — Detalhamento do domínio de restaurante e rubrica de avaliação.
- [docs/ROTEIRO.md](docs/ROTEIRO.md) — Política de tags `aula-NN-*` vs `extensao-NN-*`, log de verificações do repositório de referência e divulgação de uso de IA no desenvolvimento.

<br>

## Autoria

Desenvolvido por **Murilo Rocha Silva**  
Curso de Bacharelado em Sistemas de Informação — **UniFEF**  
GitHub: [@murilolol](https://github.com/murilolol)
