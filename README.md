# Auditor Tributário

Sistema para cálculo, conferência e auditoria de tributos, com foco inicial em empresas optantes pelo **Simples Nacional**.

O projeto nasceu com o objetivo de permitir que empresários compreendam não apenas **quanto estão pagando**, mas também:

- qual regra tributária foi aplicada;
- por que determinada atividade entrou em determinado anexo;
- qual faixa de tributação foi utilizada;
- como a alíquota foi calculada;
- quais dados participaram do cálculo;
- qual base normativa foi utilizada;
- se existem possíveis divergências entre o cálculo esperado e a guia emitida.

> **Princípio central do projeto: calcular + explicar + rastrear.**

---

## Objetivo inicial

O primeiro MVP será direcionado a prestadores de serviços sujeitos ao **Fator R**, permitindo:

1. calcular o Fator R;
2. determinar o enquadramento entre Anexo III e Anexo V;
3. determinar a faixa tributária;
4. calcular a alíquota efetiva;
5. estimar o DAS;
6. comparar o resultado com uma guia existente;
7. gerar uma memória de enquadramento;
8. explicar as decisões tributárias utilizadas pelo sistema.

O sistema não pretende substituir contador ou escrituração contábil.

Sua função inicial é atuar como uma ferramenta de **conferência, transparência e auditoria tributária**.

---

# Stack

## Backend

- Java 21
- Spring Boot
- Maven Wrapper
- JUnit
- API REST futuramente
- PostgreSQL futuramente
- Flyway futuramente

## Frontend

Planejado para uma fase posterior.

Possíveis tecnologias:

- React
- Next.js
- TypeScript

---

# Estrutura atual

```text
auditor-tributario/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── br/com/auditortributario/
│   │   │           └── taxrule/
│   │   │               ├── domain/
│   │   │               └── simples/
│   │   └── test/
│   │       └── java/
│   │           └── br/com/auditortributario/
│   ├── pom.xml
│   └── mvnw
├── docs/
│   ├── casos-de-teste/
│   └── regras-tributarias/
├── frontend/
└── README.md
```

---

# Arquitetura do motor

O sistema está sendo construído de forma que as regras tributárias não fiquem espalhadas em controllers ou condicionais sem contexto.

Fluxo desejado:

```text
Dados da empresa
       ↓
Competência
       ↓
Receitas / Folha
       ↓
Motor de regras
       ↓
Fator R
       ↓
Anexo
       ↓
Faixa tributária
       ↓
Alíquota efetiva
       ↓
Tributo estimado
       ↓
Auditoria
       ↓
Memória de enquadramento
```

---

# Explicabilidade

Todo cálculo relevante deverá produzir também uma decisão tributária.

Exemplo:

```text
Regra:
SIMPLES_FATOR_R

Entrada:
FS12 = R$ 72.000,00
RBT12 = R$ 286.000,00

Resultado matemático:
25,17482517%

Fator R considerado:
25,00%

Condição:
Fator R inferior a 28%

Enquadramento:
Anexo V

Base normativa:
Regra vigente utilizada pelo motor
```

O objetivo é permitir que o frontend apresente futuramente:

```text
Por que estou pagando isso?
```

e não apenas:

```text
Valor do imposto: R$ X
```

---

# Versionamento das regras

As regras tributárias devem possuir vigência e versão.

Exemplo conceitual:

```text
Regra:
SIMPLES_FATOR_R

Versão:
2026.1

Vigência:
competências aplicáveis àquela versão

Base normativa:
legislação e documentação oficial
```

Uma regra futura não deve sobrescrever silenciosamente uma regra histórica.

Isso permitirá recalcular corretamente competências antigas.

---

# Roadmap

## FASE 0 — Fundação

- [x] Criar estrutura do projeto
- [x] Configurar Java
- [x] Configurar Spring Boot
- [x] Configurar Maven Wrapper
- [x] Configurar Git
- [x] Criar estrutura monorepo
- [x] Executar primeiro build
- [x] Criar primeiro commit

---

## FASE 1 — Domínio tributário

- [x] Criar `SimplesAnnex`
- [x] Criar `FatorR`
- [x] Criar `TaxDecision`
- [x] Representar Anexo III
- [x] Representar Anexo V
- [x] Tratar Fator R com duas casas sem arredondamento
- [x] Criar testes unitários

---

## FASE 2 — Motor do Fator R

### FASE 2.1 — Cálculo normal

- [x] Criar `FatorRCalculator`
- [x] Calcular FS12 / RBT12
- [x] Tratar valores zerados
- [x] Determinar Anexo III ou V
- [x] Criar `FatorRCalculationResult`
- [x] Gerar `TaxDecision`
- [x] Criar testes

### FASE 2.2 — Empresas novas

- [x] Criar bases de cálculo diferentes
- [x] Tratar mês de abertura
- [x] Utilizar FSPA / RPA
- [x] Tratar empresas com menos de 13 meses
- [x] Registrar a base utilizada na decisão
- [x] Criar testes

### FASE 2.3 — Seleção automática

- [x] Criar `FatorRPeriodClassifier`
- [x] Criar `FatorRCalculationRequest`
- [x] Criar `FatorRAutomaticCalculator`
- [x] Detectar mês de abertura
- [x] Detectar empresa com menos de 13 meses
- [x] Detectar cálculo normal
- [x] Selecionar automaticamente a regra
- [x] Criar testes

---

# FASE 3 — Faixas e alíquotas

## FASE 3.1 — Tabelas tributárias

- [x] Modelar `SimplesTaxBracket`
- [x] Modelar `SimplesTaxTable`
- [x] Criar `SimplesTaxTableRegistry`
- [x] Cadastrar Anexo III
- [x] Cadastrar Anexo V
- [x] Versionar tabelas
- [x] Validar limites das faixas
- [x] Criar testes para os valores das tabelas

## FASE 3.2 — Receita para enquadramento

Próxima etapa.

- [x] Modelar histórico mensal de receitas
- [x] Calcular RBT12
- [x] Calcular RBT12 proporcionalizada
- [x] Tratar primeiro mês de atividade
- [x] Tratar os primeiros 12 meses
- [x] Detectar automaticamente a base correta
- [x] Validar meses sem faturamento
- [x] Registrar quais competências entraram no cálculo
- [x] Criar testes com exemplos oficiais

## FASE 3.3 — Seleção da faixa

- [x] Combinar Anexo + receita de enquadramento
- [x] Encontrar faixa automaticamente
- [x] Retornar alíquota nominal
- [x] Retornar parcela a deduzir
- [x] Gerar decisão tributária da faixa

---


# FASE 4 — Alíquota efetiva

- [x] Implementar fórmula da alíquota efetiva
- [x] Utilizar receita correta para cálculo
- [x] Tratar receita acumulada igual a zero
- [x] Preservar precisão interna do cálculo
- [x] Criar memória do cálculo
- [x] Criar testes por faixa
- [x] Criar testes com exemplos oficiais

Fórmula conceitual:

```text
(RBT × alíquota nominal - parcela a deduzir)
------------------------------------------------
                      RBT
```

---

# FASE 5 — Valor estimado do Simples

- [x] Receber receita tributável da competência

- [x] Aplicar alíquota efetiva

- [x] Calcular valor bruto antes do arredondamento monetário

- [x] Calcular valor monetário estimado

- [x] Validar correspondência entre competência da receita e competência da apuração

- [x] Tratar ausência de valor devido

- [x] Identificar valor inferior ao mínimo para emissão de DAS

- [x] Criar status do cálculo

- [x] Criar memória auditável do valor estimado

- [x] Criar testes para Anexo III

- [x] Criar testes para Anexo V

- [x] Criar testes de arredondamento e precisão

# Status possíveis

PAYABLE
→ Valor apto para recolhimento

DEFERRED_BELOW_MINIMUM
→ Valor inferior ao mínimo para emissão de DAS

NO_TAX_DUE
→ Sem valor devido no período
Fluxo implementado
Receita tributável do PA
          ×
Alíquota efetiva
          ↓
Valor matemático
          ↓
Valor monetário estimado
          ↓
Status da apuração
Nesta etapa, o sistema produz uma estimativa auditável para conferência. A geração oficial do DAS continua sendo realizada pelo PGDAS-D.

# FASE 6 — Auditor de guia

O módulo de auditoria compara o resultado produzido pelo motor tributário com os dados encontrados na guia ou na memória de apuração analisada.
A auditoria foi dividida em duas camadas:
AUDITORIA
    │
    ├── Valor
    │
    └── Estrutura tributária

# FASE 6.1 — Auditoria do valor

- [x] receber valor informado na guia;
- [x] comparar valor informado com valor calculado;
- [x] calcular diferença absoluta;
- [x] calcular diferença percentual;
- [x] definir tolerância para diferenças de arredondamento;
- [x] comparar enquadramento;
- [x] comparar anexo;
- [x] comparar faixa tributária;
- [x] comparar alíquota efetiva;
- [x] identificar possíveis causas da divergência;
- [x] gerar nível de severidade;
- [x] gerar decisão de auditoria;
- [x] produzir relatório explicável.
- [x] Separar precisão de cálculo da apresentação
- [x] Calcular valor monetário estimado

# Próxima etapa — FASE 6.3

Auditoria consolidada

- [x] Unificar auditoria de valor e auditoria estrutural
- [x] Criar resultado único de auditoria
- [x] Determinar severidade geral
- [x] Priorizar achados
- [x] Identificar possível causa principal
- [x] Gerar resumo executivo
- [x] Gerar lista de verificações recomendadas
- [x] Preparar estrutura para relatório
- [x] Criar testes de auditoria completa

Status consolidados
COMPATIBLE
→ Auditoria compatível

REVIEW_REQUIRED
→ Revisão adicional recomendada

DIVERGENT
→ Possível divergência
Severidades consolidadas
NONE
→ Sem divergência

LOW
→ Baixa

MEDIUM
→ Média

HIGH
→ Alta
Hipóteses principais
NONE
→ Nenhuma divergência principal identificada

FACTOR_R_OR_ANNEX
→ Possível divergência no Fator R
  ou no enquadramento tributário

REVENUE_BASIS_OR_BRACKET
→ Possível divergência na receita de
  enquadramento ou na faixa tributária

EFFECTIVE_RATE
→ Possível divergência no cálculo
  da alíquota efetiva

AMOUNT_ONLY
→ Diferença de valor sem divergência
  estrutural identificada

DEFERRED_AMOUNT
→ O valor pode depender de tributos
  diferidos de competências anteriores

INSUFFICIENT_DATA
→ Dados insuficientes para determinar
  a principal origem da diferença

## FASE 6.4 — Memória e relatório de auditoria

- [x] Criar modelo de relatório de auditoria
- [x] Gerar identificação da competência analisada
- [x] Gerar resumo executivo
- [x] Gerar memória do Fator R
- [x] Gerar memória da receita de enquadramento
- [x] Exibir Anexo e faixa tributária
- [x] Exibir alíquota nominal e parcela a deduzir
- [x] Exibir alíquota efetiva
- [x] Exibir valor estimado
- [x] Exibir comparação com valor informado
- [x] Exibir achados priorizados
- [x] Exibir principal hipótese
- [x] Exibir verificações recomendadas
- [x] Exibir referências das regras utilizadas
- [x] Gerar rastreabilidade das decisões tributárias
- [x] Criar representação estruturada do relatório
- [x] Criar renderer Markdown
- [x] Preparar estrutura para futura geração de PDF
- [x] Criar testes automatizados
---

# FASE 7 — Memória de enquadramento

Exemplo futuro:

```text
MEMÓRIA DE ENQUADRAMENTO

Regime:
Simples Nacional

Atividade:
Representação comercial

Regra:
Fator R

Folha considerada:
R$ ...

Receita considerada:
R$ ...

Fator R:
...%

Enquadramento:
Anexo ...

Faixa:
...

Alíquota nominal:
...%

Parcela a deduzir:
R$ ...

Alíquota efetiva:
...%

Valor estimado:
R$ ...
```

Também deverá existir uma visualização semelhante a:

```text
Atividade
   ↓
Regra tributária
   ↓
Fator R
   ↓
Anexo
   ↓
Faixa
   ↓
Alíquota
   ↓
DAS
```

---

# FASE 8 — API REST

Planejado:

```text
POST /api/calculations
```

Possíveis módulos:

```text
/api/companies
/api/calculations
/api/audits
/api/tax-rules
/api/reports
```

---

# FASE 9 — Frontend

Planejado:

- [ ] formulário da empresa;
- [ ] histórico mensal;
- [ ] cálculo;
- [ ] resultado;
- [ ] memória de enquadramento;
- [ ] comparação da guia;
- [ ] alertas;
- [ ] interface responsiva.

---

# FASE 10 — Documentos

Planejado:

- [ ] importar DAS;
- [ ] importar extrato do PGDAS-D;
- [ ] interpretar documentos;
- [ ] comparar dados extraídos;
- [ ] armazenar documentos com segurança.

---

# Expansões futuras

Depois do MVP:

- outros anexos do Simples Nacional;
- atividades não sujeitas ao Fator R;
- CNAEs;
- segregação de receitas;
- retenções;
- ISS;
- receitas monofásicas;
- substituição tributária;
- Lucro Presumido;
- Reforma Tributária;
- IBS;
- CBS;
- relatórios profissionais;
- histórico de auditorias.

---

# Qualidade

Regra do projeto:

> Uma regra tributária importante deve possuir teste automatizado.

Casos especialmente importantes:

- limites das faixas;
- mudanças de anexo;
- valores iguais ao limite;
- valores imediatamente acima do limite;
- empresas novas;
- valores zerados;
- mudanças de vigência;
- regras históricas.

---

# Precisão

Valores monetários são representados com:

```java
BigDecimal
```

Não utilizar:

```java
double
```

para cálculos financeiros ou tributários.

---

# Workflow

Antes de commits importantes:

```text
1. Executar testes
2. Executar build completo
3. Verificar erros de formatação/whitespace
4. Conferir git status
5. Revisar arquivos alterados
6. Fazer commit
```

Comandos de validação utilizados atualmente:

```bash
./mvnw test
./mvnw clean verify
git diff --check
git status
```

---

# Status atual

```text
Fundação                    ██████████ 100%
Domínio tributário          ██████████ 100%
Motor do Fator R            ██████████ 100%
Tabelas III e V             ██████████ 100%
Receita para enquadramento  ██████████ 100%
Seleção da faixa            ██████████ 100%
Alíquota efetiva            ██████████ 100%
Valor estimado              ██████████ 100%
Auditoria de valor          ██████████ 100%
Auditoria estrutural        ██████████ 100%
Auditoria consolidada       ██████████ 100%
Relatório de auditoria      ██████████ 100%
API                          ░░░░░░░░░░   0%
Frontend                     ░░░░░░░░░░   0%

```

---

# Visão do produto

O objetivo final não é criar apenas uma calculadora.

Queremos construir um sistema capaz de responder:

> **Quanto eu devo pagar?**

mas também:

> **Por que estou pagando isso?**

> **Qual regra foi utilizada?**

> **Qual dado levou ao meu enquadramento?**

> **Minha guia está compatível com esses dados?**

Essa explicabilidade é uma das características centrais do projeto.

---

## Aviso

O projeto possui finalidade educacional, tecnológica e de apoio à conferência tributária.

Os resultados deverão ser tratados como informações de apoio e não substituem escrituração contábil, obrigações acessórias, análise jurídica ou responsabilidade técnica de profissional habilitado.
