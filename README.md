# Restaurante 2026 — API RESTful de Gestão de Restaurantes

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot 3.4.3](https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-Schema_Migrations-2E8B57?style=flat-square&logo=liquibase&logoColor=white)](https://www.liquibase.org/)
[![Spring Security](https://img.shields.io/badge/Spring_Security-HTTP_Basic-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![JUnit 5](https://img.shields.io/badge/JUnit_5-354_tests_passed-25A162?style=flat-square&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

O **Restaurante 2026** é uma API RESTful de alto desempenho e arquitetura distribuída projetada para automatizar integralmente a operação de estabelecimentos gastronômicos. A aplicação contempla desde a gestão de cardápios e ocupação de salão até a comunicação em tempo real com a fila da cozinha, controle de comandas e fechamento financeiro de turnos de caixa.

---

## 📌 Sumário

- [Destaques de Arquitetura](#-destaques-de-arquitetura)
- [Módulos do Sistema](#-módulos-do-sistema)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura & Design Pattern](#-arquitetura--design-pattern)
- [Como Executar o Projeto](#-como-executar-o-projeto)
- [Autenticação e Controle de Acesso](#-autenticação-e-controle-de-acesso)
- [Documentação Complementar](#-documentação-complementar)
- [Autoria](#-autoria)

---

## 🚀 Destaques de Arquitetura

- **Domain-Driven Design (DDD)**: Entidades ricas com validação de invariantes e encarte de comportamentos no próprio modelo de domínio.
- **Persistência Auditável e Versionada**: Gerenciamento de esquema relacional através do Liquibase com 16 changeset migrações incrementais em YAML e validação JPA via `ddl-auto=validate`.
- **Precisão Financeira Estrita**: Emprego exclusivo de `java.math.BigDecimal` para representação de moedas e quantidades, eliminando erros de arredondamento inerentes ao padrão IEEE 754.
- **Máquinas de Estado Finitas**: Ciclos de vida orientados a estado para Pedidos (*RECEBIDO → EM_PREPARO → PRONTO → ENTREGUE / CANCELADO*), Comandas (*ABERTA → CONTA_SOLICITADA → PAGA → FECHADA*) e Caixa (*ABERTO → FECHADO*).
- **Padronização de Erros RFC 7807**: Tratamento global de exceções produzindo payloads estruturados em `application/problem+json`.
- **Fidelidade Tecnológica de Testes**: Suíte integrada com 354 testes executando contra instância real de PostgreSQL, sem dependência de bancos em memória (H2).

---

## 🧩 Módulos do Sistema

1. **Cardápio**: Categorização, precificação, seções de preparo (Cozinha/Bar) e controle de estoque.
2. **Clientes**: Cadastro unificado e histórico de consumo.
3. **Mesas**: Controle de ocupação do salão (*LIVRE*, *OCUPADA*, *RESERVADA*).
4. **Comandas**: Vinculação por mesa ou cliente, aglutinação de consumo, transferência de mesas e cálculo de conta.
5. **Pedidos**: Lançamento de itens e orquestração de transição de status.
6. **Cozinha**: Fila de preparo distribuída por seção com acompanhamento ao vivo.
7. **Caixa & Financeiro**: Abertura/fechamento de turnos, sangrias, suprimentos e quitação de pagamentos.

---

## 🛠 Tecnologias Utilizadas

- **Linguagem**: Java 21 (LTS)
- **Framework Core**: Spring Boot 3.4.3
- **Camada de Persistência**: Spring Data JPA / Hibernate 7.x
- **Banco de Dados**: PostgreSQL 16
- **Versionamento de Schema**: Liquibase
- **Segurança**: Spring Security (HTTP Basic Authentication)
- **Build & Dependency Management**: Apache Maven (via Maven Wrapper)
- **Testes Automatizados**: JUnit 5, AssertJ, MockMvc

---

## 📐 Arquitetura & Design Pattern

A aplicação adota a **Clean Architecture** dividida em 4 camadas fundamentais:

```text
com.curso.restaurante
├── config/            # Configurações de segurança, handlers RFC 7807 e inicialização
├── api/               # Controllers REST e DTOs com validação de contrato (Bean Validation)
├── domain/            # Entidades ricas de domínio, máquinas de estado e regras de negócio
├── repository/        # Repositórios Spring Data JPA e queries otimizadas
└── service/           # Camada de serviços de aplicação, orquestração e controle transacional
```

Visão detalhada com diagramas Mermaid, mapeamento E-R de 12 tabelas e fluxogramas em **[`docs/ARQUITETURA.md`](docs/ARQUITETURA.md)**.

---

## ⚙️ Como Executar o Projeto

### Pré-requisitos
- **Java 21** (JDK 21+)
- **PostgreSQL 16+** (executando localmente ou via container Docker na porta `5432`)
- **Git**

---

### 1. Preparação do Banco de Dados

Conecte-se ao seu ambiente PostgreSQL (`psql` ou pgAdmin) e crie o usuário e os bancos de dados da aplicação:

```sql
CREATE ROLE restaurante_app WITH LOGIN PASSWORD 'sua_senha_local';
CREATE DATABASE restaurante2026_dev OWNER restaurante_app;
CREATE DATABASE restaurante2026_test OWNER restaurante_app;
```

---

### 2. Configuração de Variáveis de Ambiente

Crie a sua cópia do arquivo `.env` na raiz do projeto com base no modelo `.env.example`:

```bash
cp .env.example .env
```

Ajuste as credenciais no arquivo `.env`:

```env
DB_DEV_URL=jdbc:postgresql://localhost:5432/restaurante2026_dev
DB_DEV_USERNAME=restaurante_app
DB_DEV_PASSWORD=sua_senha_local

DB_TEST_URL=jdbc:postgresql://localhost:5432/restaurante2026_test
DB_TEST_USERNAME=restaurante_app
DB_TEST_PASSWORD=sua_senha_local

ADMIN_BOOTSTRAP_PASSWORD=admin123
```

---

### 3. Inicialização do Servidor

Execute a aplicação através do Maven Wrapper:

#### Windows (PowerShell):
```powershell
.\mvnw.cmd spring-boot:run
```

#### Linux / macOS (Terminal):
```bash
./mvnw spring-boot:run
```

O Liquibase aplicará todas as migrações automaticamente no PostgreSQL e a API estará disponível no endereço:
`http://localhost:8080`

---

### 4. Execução da Suíte de Testes

Para rodar os 354 testes integrados contra o banco de dados PostgreSQL de teste:

```powershell
.\mvnw.cmd test
```

---

## 🔐 Autenticação e Controle de Acesso

A API utiliza o esquema **HTTP Basic Authentication**. No perfil de desenvolvimento (`dev`), um usuário administrador é provisionado automaticamente:

- **Usuário**: `admin`
- **Senha Padrão**: `admin123` (lida de `ADMIN_BOOTSTRAP_PASSWORD` no `.env`)

Exemplo de verificação de autenticação:
```bash
curl -u admin:admin123 http://localhost:8080/api/auth/me
```

---

## 📄 Documentação Complementar

- 🏗 **[Arquitetura do Sistema (`docs/ARQUITETURA.md`)](docs/ARQUITETURA.md)** — Diagramas de camadas, modelo de dados relacional e máquinas de estado.
- 📡 **[Especificação de API (`docs/API.md`)](docs/API.md)** — Endpoints, payloads JSON, contratos de erro RFC 7807 e paginação.
- 📋 **[Especificação do Domínio (`docs/tema-do-projeto.md`)](docs/tema-do-projeto.md)** — Regras de negócio, catálogo do cardápio e rubrica de domínio.

---

## 👨‍💻 Autoria

Desenvolvido por **Murilo Rocha Silva**  
Curso de Bacharelado em Sistemas de Informação — **UniFEF**  
GitHub: [@murilolol](https://github.com/murilolol)
