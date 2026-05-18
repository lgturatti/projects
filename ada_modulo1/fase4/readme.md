# Fase 4 — Qualidade e Testes do EventMaster

A Fase 4 concentra-se em transformar o protótipo em uma solução sustentável, testável, extensível e preparada para ambientes de produção. 

O objetivo é consolidar padrões modernos de engenharia de software, assegurando qualidade arquitetural, escalabilidade e confiabilidade operacional.

**Os principais objetivos desta fase são:**
- Aplicar os princípios SOLID;
- Implementar um Design Pattern GoF;
- Adotar práticas de Clean Code;
- Implementar testes unitários orientados por TDD;
- Demonstrar testes comportamentais utilizando BDD;
- Melhorar a manutenibilidade e a escalabilidade do sistema.

### 1. Serviço Selecionado para Refatoração

O microsserviço escolhido foi o: **Payment Service**

Esse serviço foi selecionado estrategicamente porque:
- Contém regras de negócio críticas;
- Possui múltiplos métodos de pagamento;
- Exige alta extensibilidade;
- Depende fortemente de requisitos de segurança;
- Realiza integrações frequentes com serviços externos.

### 2. Objetivos Arquiteturais

O Payment Service passa a oferecer suporte aos seguintes métodos de pagamento:
- Cartão de Crédito;
- PIX;
- PayPal;
- Novos provedores de pagamento futuros.

A arquitetura foi projetada para permitir a inclusão de novos meios de pagamento sem alterações na lógica central do sistema, demonstrando diretamente a aplicação dos seguintes princípios:

- Open/Closed Principle (OCP)
- Strategy Pattern

### 3. Aplicação dos Princípios SOLID

**S — Single Responsibility Principle (SRP)**

Cada classe possui uma única responsabilidade claramente definida.

| **Classe** | **Responsabilidade**
|---|---|
|PaymentService | Coordenação do fluxo de pagamento |
|CreditCardPayment | Processamento de cartão de crédito |
|PixPayment | Processamento via PIX |
|PaymentValidator | Validação das regras de entrada |
|PaymentRepository | Persistência de dados |

**O — Open/Closed Principle (OCP)**

Novos métodos de pagamento podem ser adicionados sem modificar classes existentes.

Exemplo:
```
ApplePayPayment
SEM modificar o PaymentService
```

**L — Liskov Substitution Principle (LSP)**

Todas as estratégias de pagamento podem ser substituídas entre si sem comprometer o comportamento do sistema.

```
PaymentStrategy strategy = new PixPayment();
```

**I — Interface Segregation Principle (ISP)**

Foram adotadas interfaces pequenas, específicas e orientadas a responsabilidades bem definidas.

**D — Dependency Inversion Principle (DIP)**

Os módulos de alto nível dependem de abstrações, e não de implementações concretas.

### O PaymentService depende da interface:

```
PaymentStrategy depende de PaymentStrategy
NÃO diretamente das classes concretas (de pagamento)
```

### 4. Design Pattern GoF — Strategy Pattern

O padrão Strategy foi escolhido por se adequar perfeitamente ao contexto de pagamentos, considerando que:
- As regras de pagamento variam;
- Os provedores possuem APIs distintas;
- As validações antifraude diferem entre métodos;
- O processamento exige comportamentos específicos por estratégia.

Esse padrão encapsula cada algoritmo de pagamento de forma independente, promovendo extensibilidade e baixo acoplamento.

### 5. Arquitetura do Strategy Pattern

img strategyPattern

### 6. Princípios de Clean Code Aplicados

|**Princípio** | **Aplicação** |
|---|---|
|Métodos Pequenos | Métodos com propósito único |
|Nomes Significativos | Classes e métodos autoexplicativos |
|Baixo Acoplamento | Uso de interfaces e abstrações |
|Alta Coesão | Responsabilidades bem definidas |
|Imutabilidade | DTOs seguros e imutáveis |
|Ausência de Duplicação | Compartilhamento de abstrações |
|Validação Defensiva | Proteção contra entradas inválidas |

### 7. Protótipo Java — SOLID + Strategy Pattern

- Requisições de pagamento [Payment Request](./PaymentRequest.java)
- Estratégias de pagamento [Payment Strategy](./PaymentStrategy.java)
- Pagamento com Cartão de Crédito [Credit Card Payment](./CreditCardPayment.java)
- Pagamento com PIX [Pix Payment](./PixPayment.java)
- Pagamento com Paypal [Paypal Payment](./PaypalPayment.java)
- Validador de pagamento [Payment Validator](./PaymentValidator.java)
- Serviço de pagamento [Payment Service](./PaymentService.java)
- Programa principal [Main](./Main.java)

### 8. TDD — Test Driven Development

O serviço agora adota a abordagem TDD (Test Driven Development) , promovendo maior confiabilidade e segurança evolutiva.

**Ciclo do TDD**
1. RED: Escrever um teste inicialmente falho.
2. GREEN: Implementar o código mínimo necessário para aprovação do teste.
3. REFACTOR: Refatorar o código mantendo a segurança garantida pelos testes.

### 9. Estratégia de Testes Unitários

A lógica de negócio validará:
- Pagamentos válidos;
- Valores inválidos;
- E-mails inválidos;
- Execução correta das estratégias de pagamento.

### 10. Testes Unitários com JUnit

- Teste do serviço de pagamento [Payment Service Test](./PaymentServiceTest.java)

### 11. Pirâmide de Testes

A estratégia de testes do EventMaster segue a abordagem da Test Pyramid , priorizando rapidez, isolamento e confiabilidade.

img testePiramide

### 12. Testes Unitários

Características:
- Rápidos;
- Isolados;
- Determinísticos;
- Dependências simuladas (_mocked_).

Exemplos:
- Validação de pagamentos;
- Geração de JWT;
- Cálculo de descontos;
- Reserva de inventário.

### 13. Testes de Integração

Validam a comunicação entre serviços distribuídos.

Exemplos:
- Sales Service ↔ Payment Service;
- Gateway ↔ Auth Service;
- Kafka Producer ↔ Consumer.

### 14. Testes End-to-End (E2E)

Validam jornadas completas do usuário.

Exemplos:
- Login;
- Adição ao carrinho;
- Compra de ingressos;
- Recebimento de notificações.

### 15. BDD — Behavioral Testing

O BDD (Behavior Driven Development) concentra-se na validação do comportamento esperado pelo negócio.

Ferramenta recomendada:
- Cucumber

### 16. Exemplo de Cenário BDD (payment.feature)

```
Feature: Ticket Payment
  Scenario: Successful credit card payment
    Given the customer has selected tickets
    And the payment amount is 200.
    When the customer pays using credit card
    Then the payment should be approved
    And the order should be confirmed
```

### 17. Step Definitions

- Etapas de pagamento [Payment Steps](./PaymentSteps.java)

### 18. Métricas de Qualidade

|**Métrica** | **Objetivo** |
|---|---|
|Cobertura de Testes Unitários | > 80% |
|Cobertura de Serviços Críticos | > 90% |
|Análise Estática | SonarQube |
|Complexidade Ciclomática | Baixa |
|Dívida Técnica | Monitorada |
|Verificação de Segurança | Obrigatória |

### 19. Análise Estática de Código

Ferramentas recomendadas:

|**Ferramenta** | **Finalidade** |
|---|---|
|SonarQube | Qualidade de código |
|PMD | Identificação de code smells |
|Checkstyle | Padronização de código |
|SpotBugs | Detecção de falhas |

### 20. Pipeline de Testes CI/CD

```
Developer Commit
↓
Unit Tests
↓
Static Analysis
↓
Security Scan
↓
Integration Tests
↓
BDD Tests
↓
Build Artifact
↓
Deploy
```

### 21. Alinhamento com Clean Architecture

Domain Layer
- PaymentRequest;
- Regras de negócio.

Application Layer
- PaymentService;
- Estratégias de pagamento.

Infrastructure Layer
- APIs de Gateway de Pagamento;
- Bancos de dados;
- Kafka.

Interface Layer
- REST Controllers;
- DTOs;
- Swagger.

### 22. Benefícios Obtidos

|**Melhoria** | **Resultado** |
|---|---|
|SOLID | Código sustentável |
|Strategy Pattern | Pagamentos extensíveis |
|TDD | Refatoração segura |
|BDD | Alinhamento com o negócio |
|Test Pyramid | Feedback rápido |
|Clean Code | Maior legibilidade |
|CI/CD Validation | Confiabilidade em produção |

### 23. Evolução Final do EventMaster

**Fase 1:**  Monólito → Microsserviços
**Fase 2:** Processamento Stream + Batch
**Fase 3:** OAuth2 + JWT + Segurança OWASP
**Fase 4:** SOLID + Design Patterns + TDD + BDD

### 24. Conclusão

A Fase 4 estabelece padrões robustos de qualidade de engenharia para o EventMaster.

O sistema agora demonstra:
- Arquitetura baseada em SOLID;
- Práticas de Clean Code;
- Implementação do Strategy Pattern;
- Testes unitários orientados por TDD;
- Validação comportamental com BDD;
- Gates de qualidade em pipelines CI/CD;
- Microsserviços sustentáveis;
- Lógica de negócio extensível.

O protótipo do Payment Service tornou-se:
- Seguro;
- Modular;
- Testável;
- Extensível;
- Preparado para produção.

Com isso, o EventMaster consolida sua evolução para uma plataforma distribuída moderna, corporativa e orientada às melhores práticas de engenharia de software, incluindo:
- Clean Architecture;
- DDD;
- Microsserviços;
- Arquitetura orientada a eventos;
- Padrões de segurança OWASP;
- Práticas modernas de desenvolvimento de software.
