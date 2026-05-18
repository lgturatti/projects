# **Fase 1:** Design e Estrutura para a Modernização do EventMaster

A solução proposta transforma a atual arquitetura monolítica em um ecossistema de
microsserviços distribuídos utilizando:

- Clean Architecture
- DDD ( _Domain-Driven Design_ )
- API Gateway
- Circuit Breaker ( _Disjuntor_ )
- Padrão SAGA
- Práticas de codificação segura
- Protótipos funcionais em Java

## 1. Visão Arquitetural

O monólito atual centraliza:
- Autenticação
- Catálogo de eventos
- Carrinho de compras
- Faturamento
- Notificações
- Inventário

Isso cria:
- Alto acoplamento
- Contenção de banco de dados
- Gargalos de escalabilidade
- Propagação de falhas
- Riscos de segurança

A proposta de modernização separa essas responsabilidades em serviços
independentes.

## 2. Diagrama de Componentes

Proposta: De Monólito para Microsserviços

Arquitetura Monolítica (Estado Atual)

img monolito


**Problemas:**

- Banco de dados relacional compartilhado
- Ponto único de falha
- Bloqueio ( _locking_ ) de banco de dados compartilhado
- Dificuldade de escalabilidade horizontal
- Acoplamento rígido
- Implantação ( _redeploy_ ) de todo o sistema para pequenas alterações
- Vulnerável a falhas em cascata

Arquitetura de Microsserviços Proposta

img microservicos

## 3. DDD - Contextos Delimitados ( _Bounded Contexts_ )

A. Contexto de Catálogo
  - **Responsável por:**
    - Cadastro de eventos
    - Inventário de ingressos
    - Preços
    - Agendas/Horários dos eventos
  - **Entidades:**
    - Evento ( _Event_ )
    - Inventário de Ingressos ( _Ticket Inventory_ )
    - Categoria ( _Category_ )
    - Local ( _Venue_ )
  - **Responsabilidades:**
    - Gerenciar o ciclo de vida do evento
    - Gerenciar assentos disponíveis
    - Publicar eventos de inventário


B. Contexto de Vendas
  - **Responsável por:**
    - Carrinho de compras
    - Checkout
    - Pagamento
    - Ciclo de vida do pedido
  - **Entidades:**
    - Carrinho ( _Cart_ )
    - Pedido ( _Order_ )
    - Pagamento ( _Payment_ )
    - Fatura/Nota Fiscal ( _Invoice_ )
  - **Responsabilidades:**
    - Criar pedidos
    - Processar pagamentos
    - Coordenar a transação SAGA

## 4. Padrões de Resiliência

API Gateway

**Onde se aplica:** Entre os clientes e os microsserviços.
- **Responsabilidades:**
  - Validação de JWT
  - Autenticação
  - Limitação de taxa ( _Rate limiting_ )
  - Agregação de requisições
  - Proteção contra tráfego malicioso
- **Benefícios de Segurança:**
  - Bloqueia requisições não autorizadas
  - Validação centralizada de tokens
  - Mitiga ataques de força bruta

Circuit Breaker ( _Disjuntor_ )

**Se aplica entre:** 
- Serviço de Vendas $\rightarrow$ Serviço de Pagamento
- Serviço de Carrinho $\rightarrow$ Serviço de Inventário

- **Objetivo:** Prevenir falhas em cascata.
- **Comportamento:** SE o serviço de pagamento falhar repetidamente: circuito
**ABERTO** ( _OPEN_ ); interrompe as requisições temporariamente e retorna uma
resposta de contingência ( _fallback_ ).
- **Biblioteca Java Recomendada:** Resilience4j

Padrão SAGA
- **Problema:** Transação distribuída:
  1.Reservar o inventário de ingressos
  2.Processar o pagamento
  3.Confirmar a compra _Se o pagamento falhar, o inventário deve ser revertido 
(rollback)._
- **Fluxo da SAGA:**
1.Pedido Criado $\rightarrow$ 2. Inventário Reservado $\rightarrow$ 3.
Pagamento Processado $\rightarrow$ 4. Pedido Confirmado $\
rightarrow$ 5. Notificação Enviada
- **Exemplo de Falha:** Pagamento falhou $\rightarrow$ Dispara compensação $\
rightarrow$ Libera o inventário reservado.

## 5. Considerações de Arquitetura Segura

Segurança de Senhas
- Nunca armazene senhas em texto puro.
- Utilize algoritmos fortes como **BCrypt** ou **Argon**.
Prevenção de SQL Injection
- Sempre utilize PreparedStatement.
- **Nunca** faça concatenação direta de strings, como:

```
String sql = "SELECT * FROM users WHERE email='" + email + "'"; 
```

Segurança de JWT

- **Boas Práticas:** Tempo de expiração curto, tokens assinados, tokens de
    atualização ( _refresh tokens_ ) e tráfego exclusivo via HTTPS.

## 6. Estrutura Proposta para o Projeto Java

Utilizando _Clean Architecture_ :

```
eventmaster/ 
├── gateway-service/
├── user-service/ 
├── catalog-service/
├── sales-service/
├── payment-service/
├── notification-service/
└── shared/ 
```

## 7. Protótipo Funcional em Java

- Camada de Domínio [Event.java](./Event.java)
- Serviço de Pagamento [PaymentService.java](./PaymentService.java)
- Simulação do Circuit Breaker [CircuitBreaker.java](./CircuitBreaker.java)
- Orquestrador SAGA [OrderSaga.java](./OrderSaga.java)
- Aplicação Principal [Main.java](./Main.java)

## 8. Exemplo de Saída da Execução
```
TENTATIVA DE COMPRA #1 
Iniciando transação SAGA... 
Inventário reservado.
Pagamento aprovado.
Pedido confirmado.
Ingressos restantes: 8

TENTATIVA DE COMPRA #2
Iniciando transação SAGA... 
Inventário reservado. 
Pagamento falhou.
Executando compensação... 
Inventário restaurado. 
Ingressos restantes: 8 
```

## 9. Mapeamento da Clean Architecture

- **Camada de Domínio**
  - Evento
  - Pedido
  - Pagamento 
Regras de negócio puras.

- **Camada de Aplicação**
  - OrderSaga
  - Casos de Uso
Coordena os fluxos de trabalho.

- **Camada de InfraEstrutura**
  - Banco de Dados
  - Mensageria
  - APIs REST
  - JWT
Integrações externas

- **Camada de Interface**
  - Controllers
  - DTOs
  - API Gateway
Trata a comunicação HTTP.

## 10. Melhorias de Segurança em Relação ao Monólito

| **Problema** | **Solução Proposta**
|---|---:|
| Injeção de SQL  | Declarações Preparadas / ORM |
| Senhas em Texto Puro |  BCrypt / Argon2 |
| Banco de Dados Compartilhado  |  Banco de dados por serviço |
| Sobrecarga do Sistema  |  Escalabilidade horizontal |
| Falhas em Cascata |  Circuit Breaker |
| Transações Inconsistentes  |  SAGA |
| Ponto Único de Falha  |  Serviços distribuídos |
| Acesso Direto Excessivo  |  API Gateway |

## 11. Tecnologias Sugeridas

| **CAMADA** | : |**Tecnologia**|
|---|:---:|---|
| **Linguagem**  |:| Java 21
| **Framework**  |:| Spring Boot
| **Segurança**  |:| Spring Security + JWT
| **Gateway** |:| Spring Cloud Gateway
| **Circuit Breaker** |:| Resilience4j
| **Mensageria** |:| RabbitMQ / Kafka
| **Banco de Dados** |:| PostgreSQL
| **Conteinerização** |:| Docker
| **Orquestração** |:| Kubernetes

## 12. Conclusão

A migração proposta moderniza o EventMaster ao:
    - Reduzir o acoplamento
    - Aumentar a escalabilidade
    - Melhorar a resiliência
    - Proteger contra ataques
    - Isolar falhas
    - Permitir implantações ( _deployments_ ) independentes
    - Suportar grandes eventos de lançamento

O protótipo funcional em Java demonstra na prática a aplicação de _Clean Architecture_ , DDD, _Circuit Breaker_ , SAGA, além de garantir a consistência de inventário e a tolerância a falhas.


