# Fase 2 – Processamento e Desempenho para o EventMaster

Na Fase 2, o objetivo é melhorar:

- Scalability -> **Escalabilidade**
- Real-time processing -> **Processamento em tempo real**
- Analytical capabilities -> **Capacidades analíticas**
- System performance -> **Desempenho do sistema**
- Fault tolerance -> **Tolerância a falhas**
- Data consistency at scale -> **Consistência de dados em escala**
A arquitetura agora evolui de apenas uma "decomposição de serviços" para uma
plataforma distribuída orientada a dados ( _data-driven_ ) utilizando:
- Processamento de Fluxo ( _Stream Processing_ )
- Processamento em Lote ( _Batch Processing_ )
- Arquitetura Orientada a Eventos ( _Event-Driven Architecture_ )
- Mensageria Assíncrona
- Pipelines de Dados Distribuídos

### 1. Estratégia do Modelo de Processamento
O ecossistema do EventMaster possui duas características de processamento
distintas:

|**Tipo de Processamento**|**Características**|**Exemplos**|
|---|---|---|
|Processamento de Fluxo (_Stream_) | Tempo real, eventos contínuos | Picos de acesso, detecção de fraude |
|Processamento em Lote (_Batch_) | Processamento agendado em larga escala | Relatórios diários, fechamento financeiro |

### 2. Arquitetura de Dados Proposta

img arquitetura_proposta

### 3. Processamento de Fluxo – Dados em Tempo Real

**Objetivo:** Processar eventos continuamente com latência mínima.

### 4. Onde o Processamento de Fluxo Será Aplicado

A. Monitoramento de Acesso em Tempo Real

- **Problema:** Grandes lançamentos de ingressos geram milhares de requisições
    simultâneas, tentativas de DDoS, ataques de bots e picos repentinos de
    tráfego.
- **Solução:** Usar Kafka Streams ou Apache Flink para processar logs em tempo
    real.
- **Fluxo do Evento:** Requisição do Cliente -> API Gateway $\
    rightarrow$ Tópico Kafka: access-events -> Processador de Fluxo
    ( _Stream Processor_ ) -> Detecção de Fraude / Métricas.

Exemplo de Dado do Fluxo (JSON)
```
{
"timestamp": "2026-05-17T21:10:00",
"ip": "192.168.1.10",
"endpoint": "/checkout",
"responseTime": 900,
"status": 429
}
```

### 5. Casos de Uso em Tempo Real

A. Detecção de Fraude
- Detectar:
  - Tentativas excessivas de login
  - Comportamento de pagamento suspeito
  - Múltiplas compras vindas do mesmo IP
  - Tráfego gerado por bots

**Regras em Tempo Real:**
```
SE tentativas_login > 10 em 1 minuto: 
   bloquear IP temporariamente
```

B. Monitoramento de Inventário de Ingressos
- Durante grandes lançamentos, o inventário muda a cada segundo e ocorrem
milhares de reservas simultâneas. O processamento de fluxo atualiza os painéis
( _dashboards_ ) instantaneamente.

C. Monitoramento de Falhas de Pagamento
- Detectar instabilidade no provedor de pagamento.

Exemplo:
```
SE falhas de pagamento excederem 20%: 
   disparar Circuit Breaker
```

D. Painéis em Tempo Real ( _Dashboards_ )
- Os organizadores podem monitorar:
  - Usuários ativos
  - Receita por minuto
  - Ingressos restantes
  - Compras concorrentes

### 6. Tecnologias de Processamento de Fluxo

|**Componente** | **Tecnologia** |
|---|---|
|Broker de Eventos |Apache Kafka |
|Mecanismo de Fluxo (_Stream_) | Kafka Streams |
|Alternativa | Apache Flink |
|Monitoramento | Prometheus + Grafana|

### 7. Protótipo em Java de Processamento de Fluxo

- Produtor de Eventos Kafka [AccessEvent](./AccessEvent.java)
- Serviço de Monitoramento em Tempo Real [RealTimeMonitor](./RealTimeMonitor.java)
- Simulação Principal do Fluxo [StreamMain](./StreamMain.java)

### 8. Processamento em Lote – Processamento Agendado

**Objetivo:** Processar grandes volumes de dados periodicamente.

Diferente do processamento de fluxo, ele:
  - Não é em tempo real
  - É focado em agregação
  - Atende a operações analíticas e financeiras

### 9. Onde o Processamento em Lote Será Aplicado

A. Relatórios Financeiros Diários

Gerar relatórios para os organizadores contendo: Total de ingressos vendidos, receita, impostos, reembolsos e taxas de pagamento.

**Frequência de Execução:**
```
Todos os dias às 02:00 AM.
```

B. Inteligência de Negócios (BI)

Agregar dados como: eventos mais vendidos, vendas regionais, horários de pico de acesso e comportamento do usuário.

C. Sincronização de Data Warehouse

Mover dados transacionais dos microsserviços do ambiente
```
OLTP -> OLAP
```

D. Logs de Auditoria

Gerar relatórios imutáveis para conformidade ( _compliance_ ).

### 10. Exemplo de Pipeline em Lote

```
PostgreSQL -> Extrair Dados -> Transformar
Transformar -> Gerar Relatório Financeiro -> Armazenar no Data Warehouse
Armazenar no Data Warehouse -> Enviar e-mail para organizadores
```

### 11. Tecnologias de Lote

|**Componente** | **Tecnologia**|
|---|---|
|Framework de Lote | Spring Batch |
|ETL em Larga Escala | Apache Spark |
|Agendador (_Scheduler_) | Quartz Scheduler |
|Armazém de Dados (_Warehouse_) | BigQuery / Snowflake |

### 12. Protótipo em Java de Processamento em Lote

- [Financial Report](./FinancialReport.java)
- [Batch Report Service](./BatchReportService.java)
- [Batch Main Simulation](./BatchMain.java)


### 13. Comparação: Fluxo (_Stream_) vs Lote (_Batch_)

|**Característica**|**Fluxo (Stream)**|**Lote (Batch)**|
|---|---|---|
|Latência | Milissegundos | Minutos/Horas |
|Processamento | Contínuo | Agendado |
|Volume de Dados |Pequenos eventos contínuos | Grandes conjuntos de dados |
|Casos de Uso | Detecção de fraude | Relatórios financeiros |
|Tecnologia | Kafka Streams | Spring Batch |
|Escalabilidade | Horizontal | Processamento paralelo |

### 14. Arquitetura Orientada a Eventos

A nova arquitetura passa a ser orientada a eventos.

- Principais Eventos
  - Pedido Criado ( _Order Created_ )
  - Pagamento Aprovado ( _Payment Approved_ )
  - Falha no Pagamento ( _Payment Failed_ )
  - Inventário Reservado ( _Inventory Reserved_ )
  - Inventário Liberado ( _Inventory Released_ )
  - Notificação Enviada ( _Notification Sent_ )

- Exemplo de Tópicos Kafka
  - access-events
  - payment-events
  - inventory-events
  - order-events
  - notification-events

### 15. Melhorias de Desempenho

| **Problema** | **Solução** |
|---|---|
|Sobrecarga no banco de dados | Bancos de dados distribuídos |
|Picos de requisições | Buffer do Kafka |
|Monitoramento em tempo real | Processamento de fluxo |
|Geração de relatórios lenta | (_Batch Pipelines_) |
|Instabilidade nos serviços | (_Circuit Breaker_) |
|Inconsistência de inventário | SAGA |
|Análises afetando a produção | Data Warehouse |

### 16. Segurança e Desempenho Juntos

- **Segurança em Fluxo** (_Stream Security_) Monitora:
  - IPs suspeitos
  - Abuso de tokens
  - Padrões anômalos de tráfego
  - Tentativas de força bruta.

- **Segurança em Lote** (_Batch_)
  - Relatórios financeiros criptografados
  - LOGs de auditoria imutáveis
  - Pipelines de ETL seguros
  - Controle de acesso para dados de BI.

### 17. Arquitetura Limpa Aplicada ao Processamento de Dados

- Camada de Fluxo ( _Stream Layer_ )
  - Produtores de Eventos
  - Tópicos Kafka
  - Consumidores
  - Análises em Tempo Real
- Camada de Lote ( _Batch Layer_ )
  - Agendadores
  - Trabalhos ( _Jobs_ ) de ETL
  - Data Warehouse
  - Relatórios de BI

## 18. Evolução Final da Arquitetura

**Fase 1:** 
```
Monolito -> Microsserviços
```

**Fase 2:**
```
Microsserviços + 
Arquitetura Orientada a Eventos +
Processamento de Fluxo + 
Análises em Lote
```

### 19. Conclusão

A Fase 2 introduz uma plataforma de dados distribuída e escalável para o
EventMaster. 

O sistema agora suporta:
- Lançamentos massivos e concorrentes
- Monitoramento em tempo real
- Prevenção de fraudes
- Análises distribuídas
- Relatórios financeiros
- Processamento assíncrono de alto desempenho

A arquitetura combina:
- Microsserviços
- Streaming de eventos com Kafka
- Pipelines de ETL em lote,
- Arquitetura Limpa
- DDD
- Orquestração SAGA
- Resiliência com Circuit Breaker

Essa evolução transforma o EventMaster em uma plataforma corporativa em nuvem (_cloud-native_), escalável, segura e observável, capaz de suportar vendas de ingressos em larga escala com confiabilidade e alta disponibilidade.
