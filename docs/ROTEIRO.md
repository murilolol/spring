# Roteiro — política de tags, reconciliação com o curso e uso de IA

Este documento registra a política de nomenclatura de commits/tags adotada neste repositório,
o histórico de verificações do repositório de referência do curso
([`suporteos2026`](https://github.com/jeffersonarpasserini/suporteos2026), Prof. Jefferson
Passerini), e como ferramentas de inteligência artificial foram usadas no desenvolvimento
das Fases 5-11 (tema pessoal).

---

## Uso de inteligência artificial no desenvolvimento

A partir da Fase 5 (tema pessoal de gestão de restaurante) e em todas as extensões
posteriores (incluindo a transferência das Aulas 05-07, ver seção própria abaixo), o
código foi escrito com o auxílio do **Claude Code** (Anthropic), um assistente de IA
operado via linha de comando, sob supervisão direta e contínua do autor. Este projeto
**não** foi gerado por um único prompt — foi construído em várias sessões de trabalho
interativas, com o autor tomando todas as decisões de escopo e revisando cada etapa.

**Decisões que couberam exclusivamente ao autor** (não à IA):

- Escolha do tema do trabalho (gestão de restaurante) e dos módulos que o compõem
  (caixa, pedidos, cozinha, clientes, comandas).
- Oito decisões de escopo explícitas, respondidas via prompts de múltipla escolha
  apresentados pela IA: avançar sem esperar a Aula 05 publicada; construir todos os
  módulos de uma vez em vez de faseado; máquina de estados completa para o Pedido;
  redesenho do cardápio do zero; comanda vinculável a mesa **ou** cliente; cozinha como
  fila própria (entidade `PreparoItem`) em vez de reaproveitar status do Pedido; caixa
  com sessão/turno completa (sangria, fechamento com conferência); autenticação básica
  (Spring Security) já nesta fase.
- Exigência de **TDD estrito** (teste falha primeiro, sempre) para todo o código novo —
  decisão tomada explicitamente pelo autor após a IA já ter escrito código de
  scaffolding sem testes; esse código foi descartado e refeito teste-primeiro por
  determinação do autor.
- Aprovação do plano de implementação detalhado antes de qualquer código ser escrito.
- Revisão final: leitura de todo o código e da documentação gerada, e decisão sobre o
  que entra no histórico público do repositório.

**O que a IA executou, sob essa direção**: escrita de entidades de domínio, serviços,
controllers REST, migrações Liquibase, testes automatizados (397 testes, todos rodando
contra PostgreSQL real — sem H2, sem mocks de repositório) e documentação técnica,
seguindo ciclo Red-Green-Refactor verificado a cada método de produção, com revisões
posteriores para manter a documentação alinhada ao código à medida que o domínio evoluiu.

**Como verificar**: o histórico de commits (`git log`) mostra a evolução incremental
real; a mensagem de cada commit descreve o que foi adicionado; `.\mvnw.cmd test`
reproduz a suíte completa localmente. O autor está à disposição para explicar qualquer
trecho do código e a lógica por trás de cada decisão de modelagem.

---

## Por que existem dois padrões de tag

O histórico deste repositório mistura dois tipos de trabalho:

- **`aula-NN-*`** — reconstruções fiéis de checkpoints reais e publicados do curso
  (Aulas 00 a 04). Essas tags **nunca são reescritas, amendadas ou rebaseadas**.
- **`extensao-NN-*`** — trabalho autoral do tema pessoal (gestão de restaurante),
  construído em cima de `aula-04-jpa-postgresql-liquibase`.

A Aula 05 do professor ainda não estava publicada no repositório de referência quando este
projeto avançou para o tema pessoal (verificado pela última vez em 2026-08-21 — sem tag, sem
pasta `docs/05aula/`, sem entrada na wiki). Por isso, o trabalho das Fases 5-11 **não** usa o
padrão `aula-05-*`: isso alegaria falsamente alinhamento com uma aula não publicada. Usa
`extensao-NN-<tema>` em vez disso, deixando claro que é conteúdo além do que foi ensinado até
o momento da implementação.

## Tags no histórico

| Tag | Conteúdo |
| :--- | :--- |
| `aula-00-inicio` a `aula-04-jpa-postgresql-liquibase` | Checkpoints fiéis das Aulas 00-04 do curso. |
| `extensao-01-seguranca-e-api-base` | Spring Security, `Usuario`, tratamento global de erros. |
| `extensao-02-cardapio-clientes-e-mesas` | Cardápio redesenhado, `Cliente`, `Mesa`. |
| `extensao-03-comandas-e-pedidos` | `Comanda`, `Pedido` com máquina de estados. |
| `extensao-04-cozinha-e-caixa` | Fila de preparo da cozinha, `SessaoCaixa`/`Sangria`/`Pagamento`. |
| `v0.2.0-gestao-restaurante` | Suíte de testes final, documentação técnica completa. |
| `v0.2.1-documentacao-final` | Revisão completa de `docs/API.md` (mapeamento de todos os endpoints e papéis, gerado a partir do `SecurityConfig` real) e `docs/ARQUITETURA.md` (diagramas de estado dos quatro agregados com máquina de estados: Pedido, Comanda, Mesa, PreparoItem), além deste `docs/ROTEIRO.md`. |
| `v0.3.0-fornecedor-e-estoque-minimo` | Transferência das Aulas 05-07: `Fornecedor`, `estoqueMinimo` em `ItemCardapio`, migração 017 via exercício real de diff do Liquibase, coleção Postman e documentação atualizada (ver seção própria abaixo). |

## Aulas 05, 06 e 07 — transferência para o tema de restaurante

O professor publicou as Aulas 05-07 em 2026-08-27 (commit `5c5039b` em
`suporteos2026`, ainda sem tags `aula-05/06/07` lá — o conteúdo virou um único
commit combinado do lado do professor). As três aulas, no tema de referência dele
(controle de estoque), implementam: repository + service transacional (Aula 05);
uma nova entidade `Fornecedor` e o campo `estoqueMinimo` em `Produto`, evoluídos via
ferramenta de diff do Liquibase (Aula 06); DTOs, mappers, controllers REST,
tratamento de erro padronizado e uma coleção Postman (Aula 07).

O próprio material do professor (`docs/tema-do-projeto.md` e
`docs/PADRAO-PEDAGOGICO.md` do repositório de referência) deixa explícito que o
exercício não é copiar os arquivos `Produto`/`Fornecedor`/`GrupoProduto` — é
aplicar a mesma estrutura conceitual ao tema escolhido por cada aluno ("uma
atividade de transferência que não seja mera cópia"). Este projeto já tinha,
antes dessas aulas serem publicadas, repository/service/controller/DTO/tratamento
de erro RFC 7807 para os 8 módulos do restaurante — bem além do escopo do que as
três aulas pedem. Por isso, em vez de recriar um pacote paralelo espelhando o tema
do professor, este incremento aplicou ao **nosso** domínio apenas o que era
genuinamente novo:

| Conceito do professor | Equivalente no restaurante | Situação |
| :--- | :--- | :--- |
| `GrupoProduto` | `CategoriaCardapio` | Já existia (extensão anterior). |
| `Produto` | `ItemCardapio` | Já existia (extensão anterior). |
| Repository + service transacional (Aula 05) | `*Repository`/`*Service` de todos os módulos | Já existia — e a lição central da Aula 05 (limite de transação, `@Transactional` ausente, `LazyInitializationException`) foi literalmente o bug real corrigido antes destas aulas serem publicadas (commit "corrige transacao ausente e associacoes lazy nao inicializadas"). |
| DTOs, mappers, controllers, erros padronizados, testes MockMvc (Aula 07) | `api/<modulo>/{controller,dto}`, `ApiExceptionHandler`/`ProblemDetail` | Já existia — usamos RFC 7807 (`ProblemDetail`) em vez do `ApiError` próprio do professor, contrato mais padronizado para o mesmo objetivo. |
| `Fornecedor` (nova entidade, Aula 06) | `domain/fornecedor/Fornecedor` | **Novo neste incremento** — fornecedor de insumos do item de cardápio (`razaoSocial`, `cnpj`, `status`). |
| `estoqueMinimo` em `Produto` (Aula 06) | `estoqueMinimo` em `ItemCardapio` | **Novo neste incremento.** |
| Evolução assistida por diff do Liquibase (Aula 06) | Migração `017-fornecedor-e-estoque-minimo.yaml` | **Novo neste incremento** — exercício de diff feito de verdade (ver abaixo). |
| Coleção Postman com cenários de erro (Aula 07 §10) | `docs/postman/restaurante2026.postman_collection.json` | **Novo neste incremento** — cobre todos os módulos (não só cardápio/fornecedor), organizada em um fluxo de atendimento completo + pastas de CRUD por módulo + cenários de erro. Validada de verdade com `npx newman run`, reexecutável (todo valor único é gerado dinamicamente). |

### Exercício de evolução de schema com ferramenta de diff (Aula 06)

Reproduzido de verdade, como a aula pede: dois bancos Postgres descartáveis
(`restaurante2026_diff`, com o schema anterior a este incremento aplicado via
Liquibase; `restaurante2026_reference`, gerado do zero pelo Hibernate
`ddl-auto=create` a partir das classes JPA já com `Fornecedor`/`estoqueMinimo`),
comparados com `./mvnw.cmd liquibase:diff` (plugin configurado em `pom.xml`,
credenciais em `.env` via `DB_DIFF_*`/`DB_REFERENCE_*`). Problemas reais
encontrados no rascunho gerado (`target/liquibase-diff/changelog-gerado.yaml`,
nunca versionado):

1. **Ruído de FK em todas as 16 chaves estrangeiras já existentes** — o Hibernate
   gera `onDelete: NO ACTION` por padrão (não lê nossas anotações `@ForeignKey`,
   que são só metadado de nome); o diff propôs derrubar e recriar todas elas.
   Nenhuma foi tocada na migração final — nossas políticas de `RESTRICT`/`CASCADE`
   já estão corretas e vivem só no Liquibase.
2. **Ruído de índice em 8 índices já existentes** (`idx_comanda_status`,
   `uk_cliente_documento`, etc.) — o Hibernate não sabe desses índices porque
   foram criados via SQL no Liquibase, não via anotação JPA; o diff propôs
   derrubá-los. Nenhum foi tocado.
3. **`estoque_minimo` gerado direto como `NOT NULL`, sem backfill** — quebraria
   contra uma tabela `item_cardapio` já com linhas. Corrigido com
   expand-migrate-contract (coluna nullable → `UPDATE ... SET estoque_minimo = 0
   WHERE estoque_minimo IS NULL` → `NOT NULL`), exatamente como a aula ensina.
4. **Nova FK `fk_item_cardapio_fornecedor` gerada com `onDelete: NO ACTION`** —
   trocada para `RESTRICT` explícito, mesma convenção do resto do projeto.
5. **Nome de chave primária genérico** (`fornecedor_pkey`, gerado pelo Postgres)
   em vez de `pk_fornecedor`, seguindo a convenção `pk_<tabela>` já usada em
   todas as outras tabelas.
6. **Nenhum `CHECK` proposto** para `fornecedor.status` nem para
   `item_cardapio.estoque_minimo >= 0` — o diff não enxerga `CHECK` (só
   estrutura de tabela/coluna/índice/FK); adicionados manualmente, mesma
   convenção SQL cru usada em todas as outras migrações.
7. **`author`/`id` gerados** (`muri (generated)`, id numérico por timestamp) —
   renomeados para `extensao-restaurante-2026` e `017-NN-descricao-curta`.
8. **Nenhum `rollback:`** no rascunho gerado — adicionado em todos os 9
   changesets finais, mesma disciplina das migrações 001-016.

Depois da migração final (`017-fornecedor-e-estoque-minimo.yaml`, 9 changesets,
mesmo padrão dos arquivos existentes), rodar o diff de novo contra um banco já
migrado mostrou só os mesmos ruídos de FK/índice conhecidos (itens 1 e 2, agora
incluindo a FK nova, esperada) — nenhuma diferença genuína, confirmando que
schema e entidades convergem.

### Segundo bug real encontrado testando a coleção Postman (Aula 07)

A coleção Postman (`docs/postman/restaurante2026.postman_collection.json`) foi
executada de verdade com o Newman (`npx newman run ...`) contra a aplicação
rodando, não só desenhada — e isso revelou outro caso da mesma classe de bug
corrigida na `Extensao 12` (associação lazy lida fora da transação), desta vez
em coleções `@OneToMany`: `Pedido.itens` e `Comanda.pedidos` continuavam
`FetchType.LAZY`, mas `PedidoResponse`/`ContaResponse` dependem delas para
montar a resposta. Qualquer endpoint que buscasse um `Pedido`/`Comanda` já
persistido (não recém-criado em memória) e devolvesse essas respostas —
`enviar-para-preparo`, `marcar-entregue`, `GET /api/pedidos`,
`GET /api/comandas/{id}/conta` — quebrava com `LazyInitializationException`.
Os testes JUnit não pegaram isso pelo mesmo motivo de sempre: a transação
única de cada `@Transactional` de teste mascara a ausência de uma transação
real no caminho controller → DTO. Corrigido trocando as duas coleções para
`FetchType.EAGER` (coleções pequenas — poucos itens por pedido, poucos
pedidos por comanda — mesmo raciocínio de escala já usado nas associações
`@ManyToOne` da `Extensao 12`). 397/397 testes continuam passando depois da
mudança, e a coleção Postman inteira (119 requisições, 150 asserções) passa
limpa, inclusive executada duas vezes seguidas sem reiniciar o banco.

### Divergência de nomenclatura nas Aulas 03/04

Comparando com o repositório de referência real, as nossas tags
`aula-03-dominio`/`aula-04-jpa-postgresql-liquibase` usam `CategoriaProduto`
onde o professor sempre usou `GrupoProduto` (confirmado lendo a árvore do
pacote `domain` na tag `aula-03-dominio` de lá) — um erro de nomenclatura de
uma reconstrução anterior a esta conversa, não uma mudança de política do
professor. Decisão: manter essas tags como estão (são tratadas como imutáveis
por política deste repositório) e só registrar a divergência aqui. Sem impacto
prático: o tema pessoal (`CategoriaCardapio`/`ItemCardapio`) nunca dependeu
desses nomes.

## Reconciliação com futuras aulas do curso

Quando o professor publicar aulas além da 07, o procedimento é:

1. Verificar primeiro se o `tema-do-projeto.md`/`PADRAO-PEDAGOGICO.md` do
   repositório de referência pedem cópia literal do exemplo dele ou
   transferência para o tema próprio (foi o caso até a Aula 07 — transferência).
2. Se for transferência: aplicar os conceitos novos diretamente ao domínio de
   restaurante já existente, como commits `Extensao NN` normais, documentando
   aqui o de-para com a aula de origem. Não fabricar tags `aula-NN-*` para
   trabalho que não seguiu a estrutura de checkpoint incremental das aulas
   reais (ver `docs/PADRAO-PEDAGOGICO.md` do professor: cada aula vira uma tag
   própria, `aula-NN-descricao`, só depois de testes passando).
3. Se for cópia literal de um exercício genérico (pouco provável, dado o
   padrão observado): aí sim criar uma branch a partir da tag do curso mais
   recente e seguir o processo de tag isolada descrito nas versões anteriores
   deste documento.

## Log de verificações do repositório de referência

| Data | Verificação | Resultado |
| :--- | :--- | :--- |
| 2026-08-21 | Busca por tag `aula-05-*`, pasta `docs/05aula/` e entrada de wiki no `suporteos2026` | Nenhum sinal de publicação da Aula 05. |
| 2026-08-27 | Nova busca no `suporteos2026` | Aulas 05, 06 e 07 publicadas em um commit único (`5c5039b`), sem tags `aula-05/06/07` ainda. Conteúdo lido na íntegra (3 documentos de aula + todo o código novo) para planejar este incremento. |
