# Roteiro — política de tags, reconciliação com o curso e uso de IA

Este documento registra a política de nomenclatura de commits/tags adotada neste repositório,
o histórico de verificações do repositório de referência do curso
([`suporteos2026`](https://github.com/jeffersonarpasserini/suporteos2026), Prof. Jefferson
Passerini), e como ferramentas de inteligência artificial foram usadas no desenvolvimento
das Fases 5-11 (tema pessoal).

---

## Uso de inteligência artificial no desenvolvimento

A partir da Fase 5 (tema pessoal de gestão de restaurante), o código foi escrito com o
auxílio do **Claude Code** (Anthropic), um assistente de IA operado via linha de comando,
sob supervisão direta e contínua do autor. Este projeto **não** foi gerado por um único
prompt — foi construído em uma sessão de trabalho interativa e longa, com o autor tomando
todas as decisões de escopo e revisando cada etapa.

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
controllers REST, migrações Liquibase, testes automatizados (354 testes, todos rodando
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

## Reconciliação futura com a Aula 05 real

Quando o professor publicar a Aula 05 no repositório de referência, o procedimento é:

1. Criar uma branch `aula-05` a partir da tag `aula-04-jpa-postgresql-liquibase`
   (**não** a partir do HEAD de `main`) — isso isola o checkpoint fiel do trabalho de extensão.
2. Implementar nessa branch somente o que a Aula 05 pedir, seguindo as convenções reais
   publicadas pelo professor (nomes de pacote, padrões de service/repository/controller).
3. Marcar essa branch com a tag `aula-05-<tema-da-aula>`.
4. Fazer merge de `aula-05` em `main`, preservando `aula-05-*` como checkpoint isolado —
   sem misturar com os commits `extensao-NN-*` já existentes.

## Log de verificações do repositório de referência

| Data | Verificação | Resultado |
| :--- | :--- | :--- |
| 2026-08-21 | Busca por tag `aula-05-*`, pasta `docs/05aula/` e entrada de wiki no `suporteos2026` | Nenhum sinal de publicação da Aula 05. |
