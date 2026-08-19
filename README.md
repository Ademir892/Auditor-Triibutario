# Auditor Tributário

Plataforma para **cálculo, conferência, explicação e auditoria tributária**.

O objetivo do projeto é permitir que uma pessoa ou empresa possa revisar uma apuração tributária, conferir uma guia mensal ou anual, obter uma segunda opinião independente e, principalmente, entender:

> **Por que estou pagando isso?**

O Auditor Tributário não pretende apenas retornar um valor.

A proposta é reconstruir a lógica tributária utilizada, registrar a memória das decisões, comparar os resultados com os dados informados e indicar possíveis pontos de divergência.

---

# Visão do produto

O Auditor Tributário está sendo desenvolvido para executar cinco funções principais:

```text
CALCULAR
   ↓
EXPLICAR
   ↓
RASTREAR
   ↓
COMPARAR
   ↓
AUDITAR
```

A aplicação deve ser capaz de responder perguntas como:

- Qual regra tributária foi aplicada?
- Qual foi a base utilizada no cálculo?
- Por que determinado anexo foi escolhido?
- Qual faixa tributária foi aplicada?
- Como a alíquota efetiva foi calculada?
- Quanto o sistema estima que deveria ser recolhido?
- Quanto foi informado na guia?
- Existe divergência?
- Onde está a divergência?
- Qual ponto deveria ser investigado primeiro?
- Quais informações ainda estão faltando?
- Como explicar o cálculo para quem está pagando o tributo?

---

# Objetivo de longo prazo

O projeto não ficará restrito a um único nicho, atividade ou tipo de contribuinte.

A visão é construir uma plataforma capaz de atender diferentes situações tributárias e diferentes perfis de usuários.

O usuário poderá utilizar o sistema para:

- conferir uma guia mensal;
- revisar um período anual;
- solicitar uma segunda opinião;
- investigar uma possível divergência;
- entender a composição de um tributo;
- reconstruir a memória de uma apuração;
- comparar valores informados com valores recalculados;
- documentar uma auditoria;
- acompanhar várias competências dentro do mesmo caso.

---

# Escopo tributário atual

Neste momento, o motor tributário implementado está concentrado no:

```text
Simples Nacional
└── serviços sujeitos ao Fator R
    ├── Anexo III
    └── Anexo V
```

O sistema atualmente calcula:

- classificação temporal da empresa;
- Fator R;
- enquadramento entre Anexo III e Anexo V;
- RBT12;
- RBT12 proporcionalizada;
- faixa tributária;
- alíquota nominal;
- parcela a deduzir;
- alíquota efetiva;
- valor mensal estimado;
- situação do valor calculado;
- comparação com valor informado;
- auditoria estrutural;
- auditoria consolidada;
- hipótese principal de divergência;
- recomendações de conferência;
- memória das decisões tributárias;
- relatório estruturado de auditoria.

---

# Fator R

O sistema calcula o Fator R com base na relação entre folha e receita.

```text
Fator R =
Folha considerada
──────────────────
Receita considerada
```

A regra atualmente modelada é:

```text
Fator R >= 28%
        ↓
    Anexo III

Fator R < 28%
        ↓
     Anexo V
```

O valor utilizado na decisão é tratado com duas casas decimais sem arredondamento.

Exemplo:

```text
Fator bruto:
27,74%

Valor considerado:
27%

Resultado:
Anexo V
```

---

# Regra temporal da empresa

O sistema identifica automaticamente o tempo de existência da empresa em relação à competência analisada.

Existem três situações:

```text
OPENING_MONTH
Mês de abertura

UNDER_13_MONTHS
Empresa com menos de 13 meses

STANDARD_12_MONTHS
Empresa com histórico normal de 12 meses
```

Essa classificação influencia as bases utilizadas nos cálculos.

---

# Receita utilizada para enquadramento

O motor calcula automaticamente a base utilizada para determinar a faixa tributária.

## Primeiro mês

```text
RBT12p =
Receita da competência × 12
```

Exemplo:

```text
Receita:
R$ 20.000,00

RBT12p:
R$ 240.000,00
```

## Empresa com menos de 13 meses

O sistema utiliza a média das receitas anteriores desde a abertura e proporcionaliza o resultado para 12 meses.

Exemplo:

```text
Fevereiro:
R$ 10.000,00

Março:
R$ 0,00

Abril:
R$ 590.000,00

Total:
R$ 600.000,00

Média:
R$ 200.000,00

RBT12p:
R$ 2.400.000,00
```

O sistema diferencia:

```text
mês inexistente
```

de:

```text
mês existente com receita R$ 0,00
```

Receita zero é um dado válido.

Mês ausente representa falta de informação.

## A partir do 13º mês

A base utilizada passa a ser:

```text
RBT12 =
soma das receitas dos 12 meses anteriores
```

---

# Anexos atualmente implementados

## Anexo III

| Faixa | Receita máxima | Alíquota nominal | Parcela a deduzir |
|---|---:|---:|---:|
| 1 | R$ 180.000 | 6,00% | R$ 0 |
| 2 | R$ 360.000 | 11,20% | R$ 9.360 |
| 3 | R$ 720.000 | 13,50% | R$ 17.640 |
| 4 | R$ 1.800.000 | 16,00% | R$ 35.640 |
| 5 | R$ 3.600.000 | 21,00% | R$ 125.640 |
| 6 | R$ 4.800.000 | 33,00% | R$ 648.000 |

## Anexo V

| Faixa | Receita máxima | Alíquota nominal | Parcela a deduzir |
|---|---:|---:|---:|
| 1 | R$ 180.000 | 15,50% | R$ 0 |
| 2 | R$ 360.000 | 18,00% | R$ 4.500 |
| 3 | R$ 720.000 | 19,50% | R$ 9.900 |
| 4 | R$ 1.800.000 | 20,50% | R$ 17.100 |
| 5 | R$ 3.600.000 | 23,00% | R$ 62.100 |
| 6 | R$ 4.800.000 | 30,50% | R$ 540.000 |

---

# Versionamento das regras

As tabelas tributárias são versionadas.

O sistema não deve utilizar automaticamente uma regra antiga em competências futuras.

O escopo atual das tabelas foi deliberadamente limitado até:

```text
12/2026
```

Se uma competência futura não possuir uma regra validada, o cálculo deve falhar de forma controlada em vez de utilizar silenciosamente uma tabela desatualizada.

---

# Alíquota efetiva

A alíquota efetiva é calculada utilizando:

```text
(RBT12 × alíquota nominal − parcela a deduzir)
──────────────────────────────────────────────
                    RBT12
```

Exemplo:

```text
RBT12:
R$ 500.000,00

Alíquota nominal:
13,50%

Parcela a deduzir:
R$ 17.640,00
```

Resultado:

```text
Alíquota efetiva:
9,972%
```

A aplicação preserva alta precisão internamente e realiza arredondamentos apenas quando necessário para apresentação monetária.

---

# Valor mensal estimado

Após encontrar a alíquota efetiva:

```text
Valor estimado =
Receita tributável da competência
×
Alíquota efetiva
```

Exemplo:

```text
Receita:
R$ 10.000,00

Alíquota efetiva:
9,972%

Valor estimado:
R$ 997,20
```

O motor diferencia:

```text
rawTaxAmount
```

e:

```text
estimatedTaxAmount
```

permitindo preservar o valor matemático antes do arredondamento monetário.

---

# Status do valor estimado

O sistema possui três estados:

```text
PAYABLE
Valor apto para recolhimento

DEFERRED_BELOW_MINIMUM
Valor abaixo do mínimo considerado

NO_TAX_DUE
Nenhum valor devido
```

---

# Auditoria do valor da guia

O sistema pode comparar o valor calculado com o valor informado.

Exemplo:

```text
Valor esperado:
R$ 997,20

Valor informado:
R$ 1.752,00

Diferença:
R$ 754,80
```

O resultado informa:

- diferença assinada;
- diferença absoluta;
- diferença percentual;
- se o valor informado está acima do esperado;
- se o valor informado está abaixo do esperado;
- tolerância utilizada;
- status da comparação.

Status possíveis:

```text
EXACT_MATCH

WITHIN_TOLERANCE

DIVERGENT

REQUIRES_ADDITIONAL_CONTEXT
```

A tolerância padrão atual é:

```text
R$ 0,05
```

Essa tolerância é uma heurística técnica interna do software e não uma regra legal.

---

# Auditoria estrutural

Além do valor, o sistema compara elementos da estrutura tributária.

Atualmente são auditados:

- Fator R;
- Anexo;
- faixa;
- alíquota efetiva.

Exemplo:

```text
Calculado pelo sistema

Fator R:
30%

Anexo:
III

Faixa:
3

Alíquota efetiva:
9,972%
```

contra:

```text
Informado

Fator R:
25%

Anexo:
V

Faixa:
3

Alíquota efetiva:
17,52%
```

Possíveis achados:

```text
FATOR_R_MISMATCH
ANNEX_MISMATCH
BRACKET_MISMATCH
EFFECTIVE_RATE_MISMATCH
```

Também existem achados para dados não informados:

```text
FATOR_R_NOT_REPORTED
ANNEX_NOT_REPORTED
BRACKET_NOT_REPORTED
EFFECTIVE_RATE_NOT_REPORTED
```

Informação ausente não é automaticamente considerada informação errada.

---

# Severidade dos achados

As severidades atuais são:

```text
NONE
LOW
MEDIUM
HIGH
```

São classificações internas do software.

Não representam classificação oficial da Receita Federal ou da legislação tributária.

---

# Auditoria consolidada

O sistema combina:

```text
Auditoria de valor
        +
Auditoria estrutural
```

para gerar um diagnóstico único.

Exemplo:

```text
Valor             ⚠ divergente
Fator R           ⚠ divergente
Anexo             ⚠ divergente
Faixa             ✓ compatível
Alíquota efetiva  ⚠ divergente
```

Resultado:

```text
Status:
DIVERGENT

Severidade:
HIGH
```

---

# Hipótese principal de divergência

O Auditor Tributário também tenta priorizar onde a investigação deve começar.

Possíveis causas:

```text
NONE

FACTOR_R_OR_ANNEX

REVENUE_BASIS_OR_BRACKET

EFFECTIVE_RATE

AMOUNT_ONLY

DEFERRED_AMOUNT

INSUFFICIENT_DATA
```

Exemplo:

```text
Fator R divergente
+
Anexo divergente
+
Valor divergente
```

pode gerar:

```text
Principal hipótese:
FACTOR_R_OR_ANNEX
```

Essa classificação representa uma hipótese de auditoria.

Não significa automaticamente que o contador ou responsável pela apuração cometeu um erro.

---

# Recomendações de conferência

O sistema já consegue gerar recomendações como:

- conferir folha utilizada no Fator R;
- conferir pró-labore e encargos;
- conferir receitas;
- conferir competências;
- conferir RBT12 ou RBT12p;
- conferir anexo;
- conferir faixa;
- conferir alíquota nominal;
- conferir parcela a deduzir;
- conferir alíquota efetiva.

---

# Rastreabilidade

Uma das características centrais do projeto é a memória das decisões tributárias.

As principais etapas produzem um:

```text
TaxDecision
```

contendo:

```text
ruleCode
ruleVersion
description
input
condition
result
legalReference
```

Isso permite reconstruir posteriormente:

```text
qual regra foi aplicada
+
qual versão estava vigente
+
quais dados entraram
+
qual condição foi satisfeita
+
qual resultado foi produzido
+
qual referência estava associada
```

---

# Relatório de auditoria

O sistema já possui um modelo estruturado de relatório.

As seções atuais são:

```text
SUMMARY

TAX_FRAMEWORK

AMOUNT_COMPARISON

FINDINGS

RECOMMENDATIONS

TRACEABILITY
```

Também existe renderização em Markdown.

O relatório poderá futuramente alimentar:

- frontend;
- PDF;
- compartilhamento;
- histórico da auditoria;
- relatórios anuais;
- exportações.

---

# API HTTP

O backend é desenvolvido utilizando Java e Spring Boot.

Endpoints atuais:

## Health check

```http
GET /api/health
```

Exemplo:

```json
{
  "status": "UP",
  "application": "auditor-tributario"
}
```

---

## Cálculo tributário

```http
POST /api/v1/simples/calculate
```

Exemplo de entrada:

```json
{
  "openingDate": "2026-02-10",
  "assessmentPeriod": "2026-02",
  "fatorRPayrollBase": 6000.00,
  "fatorRRevenueBase": 20000.00,
  "taxableRevenue": 20000.00,
  "priorMonthlyRevenues": []
}
```

O endpoint retorna dados como:

```text
competência
Fator R
base temporal
Anexo
RBT12 / RBT12p
faixa
alíquota nominal
parcela a deduzir
alíquota efetiva
receita tributável
valor estimado
status do valor
versão da tabela
```

---

# API de auditoria

```http
POST /api/v1/simples/audit
```

Além dos dados utilizados no cálculo, podem ser informados:

```text
valor da guia
Fator R informado
Anexo informado
faixa informada
alíquota efetiva informada
```

O resultado pode conter:

```text
status geral

severidade

valor esperado

valor informado

diferença

auditoria estrutural

achados

principal hipótese

resumo executivo

recomendações
```

Uma divergência tributária é um resultado válido da auditoria.

Portanto:

```text
DIVERGENT
```

não representa necessariamente erro HTTP.

---

# API de relatório

```http
POST /api/v1/simples/audit/report
```

O endpoint recebe os mesmos dados da auditoria e retorna:

- competência;
- status;
- severidade;
- principal hipótese;
- título;
- resumo executivo;
- seções do relatório;
- referências;
- versão Markdown.

Esse endpoint fecha o primeiro fluxo completo do produto:

```text
ENTRADA
  ↓
CÁLCULO
  ↓
AUDITORIA
  ↓
DIAGNÓSTICO
  ↓
EXPLICAÇÃO
  ↓
RELATÓRIO
```

---

# Tratamento de erros da API

Existe tratamento global para erros de entrada.

Exemplos:

```text
campo obrigatório ausente
valor negativo
JSON inválido
competência inválida
empresa ainda não aberta
histórico inconsistente
regra incompatível
```

Erros de entrada retornam respostas estruturadas com:

```text
timestamp
status
error
message
path
fieldErrors
```

O sistema deliberadamente não transforma todas as exceções em `400`.

Falhas reais da aplicação devem continuar aparecendo como erros internos.

---

# Nova arquitetura de auditoria

O projeto agora possui um conceito independente de:

```text
AuditCase
```

Um caso representa uma investigação tributária.

Ele pode ser:

```text
MONTHLY

ANNUAL

SECOND_OPINION

EXPLANATORY_REVIEW
```

Isso permite representar cenários como:

```text
"Confira minha guia deste mês"

"Revise meu ano inteiro"

"Quero uma segunda opinião"

"Quero entender por que estou pagando isso"
```

---

# Regimes tributários

O modelo já admite os seguintes regimes:

```text
SIMPLES_NACIONAL

LUCRO_PRESUMIDO

LUCRO_REAL

MEI

OTHER
```

Atualmente somente o motor do:

```text
SIMPLES_NACIONAL
```

está implementado.

Os demais fazem parte da arquitetura futura.

---

# Sujeito auditado

O projeto também possui um modelo para representar quem está sendo analisado.

```text
AuditedSubject
```

Pode representar:

```text
INDIVIDUAL

BUSINESS

OTHER
```

O sujeito auditado possui:

```text
ID interno

tipo

nome de exibição

identificador tributário opcional
```

Identificadores atualmente modelados:

```text
CPF

CNPJ

OTHER
```

A estrutura do CNPJ aceita formato tradicional e alfanumérico.

O projeto não utiliza tipos numéricos para armazenar identificadores tributários.

---

# Identificação opcional

Um caso poderá futuramente começar mesmo sem CPF ou CNPJ.

Isso permite experiências como:

```text
"Quero apenas entender esta guia."
```

O sistema pode iniciar a análise e solicitar informações adicionais apenas quando forem necessárias.

---

# Período de auditoria

O sistema possui o conceito:

```text
AuditPeriod
```

que pode representar:

```text
01/2026 → 01/2026
```

ou:

```text
01/2026 → 12/2026
```

ou qualquer intervalo contínuo:

```text
10/2026 → 02/2027
```

---

# Competências do caso

Cada caso possui suas competências.

Exemplo:

```text
Auditoria anual de 2026

01/2026
02/2026
03/2026
04/2026
05/2026
06/2026
07/2026
08/2026
09/2026
10/2026
11/2026
12/2026
```

Cada competência possui estado próprio:

```text
PENDING

IN_PROGRESS

COMPLETED

REQUIRES_INFORMATION
```

Isso prepara o sistema para cenários como:

```text
Janeiro       ✅ concluído
Fevereiro     ✅ concluído
Março         ⚠ aguardando informações
Abril         🔄 em análise
Maio          ⏳ pendente
```

---

# Estrutura arquitetural

A aplicação está sendo organizada aproximadamente assim:

```text
backend
└── src
    ├── main
    │   └── java
    │       └── br.com.auditortributario
    │
    │           ├── api
    │           │   ├── error
    │           │   ├── health
    │           │   └── simples
    │           │       ├── audit
    │           │       └── calculation
    │           │
    │           ├── application
    │           │   └── simples
    │           │       └── audit
    │           │
    │           ├── auditcase
    │           │   └── subject
    │           │
    │           └── taxrule
    │               ├── domain
    │               └── simples
    │
    └── test
        └── java
            └── br.com.auditortributario
                ├── api
                ├── auditcase
                └── taxrule
```

A separação de responsabilidades segue:

```text
api
↓
HTTP e transporte

application
↓
orquestração de casos de uso

auditcase
↓
domínio da investigação tributária

taxrule
↓
regras e cálculos tributários
```

---

# Princípios de arquitetura

O projeto segue alguns princípios importantes.

## Regra tributária não pertence ao controller

```text
Controller
↓
Service
↓
Domain
```

Controllers devem apenas receber e devolver dados HTTP.

---

## Informação ausente não deve ser inventada

Se determinado dado não estiver disponível:

```text
não informado
```

é diferente de:

```text
incorreto
```

---

## Competências são independentes

Uma auditoria anual será construída através da consolidação de competências mensais.

Não haverá uma segunda implementação completamente diferente apenas para o anual.

---

## Regras tributárias devem ser versionadas

Mudanças legislativas não devem alterar silenciosamente cálculos antigos.

---

## Cálculo precisa ser explicável

Não basta produzir:

```text
R$ 997,20
```

O sistema precisa conseguir mostrar:

```text
como
+
por que
+
com qual regra
```

chegou a esse valor.

---

# Roadmap

## FASE 0 — Fundação

- [x] Estrutura inicial do projeto
- [x] Backend Java
- [x] Spring Boot
- [x] Maven Wrapper
- [x] Testes automatizados
- [x] Git / GitHub

---

## FASE 1 — Domínio tributário inicial

- [x] Modelo de decisão tributária
- [x] Estrutura de regras versionadas
- [x] Tipos fundamentais do Simples

---

## FASE 2 — Fator R

### 2.1 Cálculo normal

- [x] Cálculo do Fator R
- [x] Truncamento
- [x] Anexo III / V

### 2.2 Empresas novas

- [x] Mês de abertura
- [x] Menos de 13 meses
- [x] Casos especiais de folha e receita

### 2.3 Seleção automática da base

- [x] Classificação temporal
- [x] Seleção automática da regra

---

## FASE 3 — Tabelas e faixas

### 3.1 Tabelas

- [x] Anexo III
- [x] Anexo V
- [x] Versionamento
- [x] Proteção para competências futuras

### 3.2 Receita de enquadramento

- [x] RBT12
- [x] RBT12p
- [x] Empresa no primeiro mês
- [x] Empresa com menos de 13 meses
- [x] Histórico de 12 meses

### 3.3 Seleção da faixa

- [x] Seleção automática
- [x] Alíquota nominal
- [x] Parcela a deduzir

---

## FASE 4 — Alíquota efetiva

- [x] Fórmula
- [x] Precisão interna
- [x] Rastreabilidade
- [x] Testes

---

## FASE 5 — Valor estimado

- [x] Receita tributável
- [x] Valor matemático
- [x] Valor monetário
- [x] Valor mínimo
- [x] Status do resultado
- [x] Testes

---

## FASE 6 — Auditoria

### 6.1 Auditoria de valor

- [x] Comparação da guia
- [x] Diferença absoluta
- [x] Diferença percentual
- [x] Tolerância

### 6.2 Auditoria estrutural

- [x] Fator R
- [x] Anexo
- [x] Faixa
- [x] Alíquota efetiva
- [x] Informações ausentes
- [x] Severidades

### 6.3 Auditoria consolidada

- [x] Consolidação dos resultados
- [x] Achados priorizados
- [x] Severidade geral
- [x] Principal hipótese
- [x] Resumo executivo
- [x] Recomendações

### 6.4 Relatório

- [x] Modelo de relatório
- [x] Competência
- [x] Resumo executivo
- [x] Memória tributária
- [x] Comparação
- [x] Achados
- [x] Recomendações
- [x] Rastreabilidade
- [x] Referências
- [x] Markdown
- [x] Estrutura preparada para PDF

---

## FASE 7 — API

### 7.1 Bootstrap

- [x] Spring MVC
- [x] Health check
- [x] Teste HTTP

### 7.2 Cálculo

- [x] Request DTO
- [x] Response DTO
- [x] Application Service
- [x] Endpoint de cálculo
- [x] Testes HTTP

### 7.3 Erros

- [x] Bean Validation
- [x] GlobalExceptionHandler
- [x] JSON inválido
- [x] Erros de domínio
- [x] Respostas estruturadas

### 7.4 Auditoria

- [x] API de auditoria
- [x] Application Service
- [x] Auditoria de valor
- [x] Auditoria estrutural
- [x] Auditoria consolidada
- [x] Testes HTTP

### 7.5 Relatório

- [x] Serviço de relatório
- [x] Endpoint de relatório
- [x] Representação estruturada
- [x] Markdown
- [x] Testes HTTP

---

## FASE 8 — Plataforma de auditoria

### 8.1 Caso de auditoria

- [x] AuditCaseId
- [x] AuditCaseType
- [x] TaxRegime
- [x] AuditPeriod
- [x] AuditCaseStatus
- [x] AuditCase

### 8.2 Sujeito auditado

- [x] AuditedSubjectId
- [x] AuditedSubjectType
- [x] TaxIdentifierType
- [x] TaxIdentifier
- [x] CPF
- [x] CNPJ tradicional
- [x] CNPJ alfanumérico
- [x] Identificação opcional
- [x] AuditedSubject

### 8.3 Competências

- [x] AuditCompetence
- [x] AuditCompetenceStatus
- [x] Geração automática pelo período
- [x] Caso mensal
- [x] Caso anual
- [x] Intervalos personalizados
- [x] Vínculo AuditCase ↔ AuditedSubject
- [x] Busca de competência
- [x] Validação das competências

### 8.4 Evidências e documentos

Próxima etapa.

- [ ] Documento do caso
- [ ] Documento da competência
- [ ] Tipo do documento
- [ ] Origem do dado
- [ ] Evidência tributária
- [ ] Guia
- [ ] Apuração
- [ ] Declaração
- [ ] Comprovante
- [ ] Metadados
- [ ] Histórico
- [ ] Preparação para upload
- [ ] Preparação para PDF
- [ ] Preparação para OCR

---

# Próximas grandes capacidades tributárias

O roadmap de longo prazo inclui:

- [ ] segregação completa de diferentes tipos de receita;
- [ ] mais de uma atividade/anexo na mesma competência;
- [ ] repartição entre IRPJ;
- [ ] CSLL;
- [ ] Cofins;
- [ ] PIS/Pasep;
- [ ] CPP;
- [ ] ISS;
- [ ] ICMS;
- [ ] retenções tributárias;
- [ ] ISS retido;
- [ ] substituição tributária;
- [ ] receitas monofásicas;
- [ ] tributação concentrada;
- [ ] isenções;
- [ ] reduções;
- [ ] receitas de exportação;
- [ ] sublimites estaduais;
- [ ] excesso de sublimite;
- [ ] Anexo I;
- [ ] Anexo II;
- [x] Anexo III no escopo atual;
- [ ] Anexo IV;
- [x] Anexo V no escopo atual;
- [ ] MEI;
- [ ] Lucro Presumido;
- [ ] Lucro Real;
- [ ] parcelamentos;
- [ ] juros;
- [ ] multas.

---

# Plataforma futura

Também estão planejados:

- [ ] banco de dados;
- [ ] persistência de casos;
- [ ] histórico de auditorias;
- [ ] autenticação;
- [ ] usuários;
- [ ] frontend;
- [ ] dashboard;
- [ ] upload de documentos;
- [ ] leitura automática de PDF;
- [ ] OCR;
- [ ] relatórios PDF;
- [ ] auditoria anual consolidada;
- [ ] comparação entre anos;
- [ ] acompanhamento de divergências recorrentes.

---

# Integrações futuras

Em etapas mais avançadas poderão ser estudadas:

- [ ] geração oficial do DAS;
- [ ] integração com PGDAS-D;
- [ ] transmissão de informações;
- [ ] integração direta com Receita Federal.

Essas funcionalidades não são prioridade imediata.

A prioridade atual é construir um motor confiável para:

```text
CALCULAR
+
EXPLICAR
+
AUDITAR
+
DOCUMENTAR
```

---

# Limites atuais

O sistema ainda não substitui uma apuração completa realizada no PGDAS-D.

O valor produzido pelo motor deve ser apresentado como:

> **Valor estimado pelo motor para conferência.**

E não como:

> **Valor oficial necessariamente devido.**

O produto é atualmente uma ferramenta de:

```text
conferência
segunda opinião
explicação
investigação
auditoria
```

e não um substituto oficial dos sistemas da administração tributária.

---

# Tecnologias

## Backend

```text
Java
Spring Boot
Spring MVC
Bean Validation
Maven
JUnit
MockMvc
```

## Persistência

Ainda não implementada.

Planejada:

```text
PostgreSQL
Flyway
```

## Frontend

Ainda não implementado.

---

# Executando o backend

Entre na pasta:

```bash
cd backend
```

Execute os testes:

```bash
./mvnw test
```

Execute a verificação completa:

```bash
./mvnw clean verify
```

Inicie a aplicação:

```bash
./mvnw spring-boot:run
```

Por padrão:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/api/health
```

---

# Qualidade antes dos commits

Antes de commits importantes:

```bash
./mvnw clean verify
git diff --check
git status
```

Depois do stage:

```bash
git add .
git diff --cached --check
git status
```

Somente depois das verificações o commit deve ser realizado.

---

# Estado atual

```text
Fundação                       ██████████ 100%

Motor tributário inicial       ██████████ 100%

Fator R                        ██████████ 100%

Anexos III e V                 ██████████ 100%

RBT12 / RBT12p                 ██████████ 100%

Faixa tributária               ██████████ 100%

Alíquota efetiva               ██████████ 100%

Valor estimado                 ██████████ 100%

Auditoria de valor             ██████████ 100%

Auditoria estrutural           ██████████ 100%

Auditoria consolidada          ██████████ 100%

Relatório                      ██████████ 100%

API inicial                    ██████████ 100%

Caso de auditoria              ██████████ 100%

Sujeito auditado               ██████████ 100%

Competências do caso           ██████████ 100%

Evidências/documentos          ░░░░░░░░░░   0%

Persistência                   ░░░░░░░░░░   0%

Frontend                       ░░░░░░░░░░   0%

Expansão tributária            ░░░░░░░░░░   0%
```

---

# Direção do projeto

O Auditor Tributário está deixando de ser apenas:

```text
uma calculadora do Simples Nacional
```

para se tornar:

```text
uma plataforma de investigação tributária
```

O objetivo final é permitir que qualquer usuário consiga responder:

> Quanto estou pagando?

> Por que estou pagando?

> Como esse valor foi calculado?

> Essa apuração parece coerente?

> Existe alguma divergência?

> Onde devo investigar primeiro?

> Quais documentos sustentam essa conclusão?

Esse é o norte do projeto.