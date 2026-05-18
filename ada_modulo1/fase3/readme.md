# Fase 3 - Arquitetura de Segurança para o EventMaster

A Fase 3 concentra-se em transformar o EventMaster em uma plataforma
distribuída segura de nível corporativo usando:

- OAuth 2.
- Autenticação JWT
- Princípios de Confiança Zero ( _Zero Trust_ )
- Estratégias de mitigação do OWASP Top 10
- Comunicação segura de microsserviços
- CI/CD seguro e cadeia de suprimentos de software

Esta fase integra a segurança diretamente em:
- Arquitetura
- Infraestrutura
- APIs
- Autenticação
- Autorização
- Proteção de dados
- Pipelines de implantação

### 1. Visão Geral da Arquitetura de Segurança

A arquitetura proposta adota:
- Servidor de Autorização OAuth 2.
- Autenticação sem estado ( _stateless_ ) via JWT
- Aplicação de segurança no API Gateway
- RBAC (Controle de Acesso Baseado em Função)
- Ciclo de vida seguro de tokens
- Comunicação segura entre serviços

### 2. Fluxo de Autenticação Segura (OAuth 2.0 + JWT)

Arquitetura de Login Proposta
    - App Cliente (Web / Mobile): Envia a Solicitação de Login.
    - API Gateway: Responsável por Limitação de Taxa ( _Rate Limiting_ ), WAF e
       Validação JWT.
    - Serviço de Autenticação: Realiza a Autorização OAuth 2.0, valida credenciais,
       gera JWT e cuida dos Tokens de Atualização, conectando-se ao Banco de Dados
       de Usuários.

### 3. Fluxo OAuth 2.

O fluxo recomendado para o EventMaster: **Fluxo de Código de Autorização
(** **_Authorization Code Flow_** **) + PKCE**

Esta é a opção mais segura para:
    - Aplicações Web
    - Aplicativos móveis
    - SPA (Aplicações de Página Única)

### 4. Sequência de Autenticação
    1. O usuário envia as credenciais.
    2. O API Gateway encaminha a solicitação.
    3. O Serviço de Autenticação valida as credenciais.
    4. Verificação do hash da senha (BCrypt).
    5. O servidor OAuth emite:
       - Access Token (Token de Acesso JWT)
       - Refresh Token (Token de Atualização)
    6. O cliente armazena o token de forma segura.
    7. O JWT é enviado em cada solicitação.
    8. O Gateway valida a assinatura do JWT.
    9. A solicitação é roteada para o microsserviço.

### 5. Estrutura do JWT

Exemplo de _payload_ (JSON):
```
{
"sub": "12345",
"email": "user@eventmaster.com",
"role": "CUSTOMER",
"iat": 1715900000,
"exp": 1715903600
}
```

### 6. Controles de Segurança do JWT

|**Controle** | **Propósito** |
|---|---|
|Limites de Tempo de Expiração | Limita o uso de token roubado |
|Tokens Assinados | Evita adulteração ( _tampering_ ) |
|Tokens de Atualização | Reautenticação segura |
|Apenas HTTPS | Previne a interceptação do token |
|Cookies Seguros | Previne roubo via XSS |
|Rotação de Token | Previne ataques de repetição ( _replay attacks_ ) |

### 7. Comunicação Segura de Microsserviços

Os serviços internos se comunicam através de: **mTLS (TLS Mútuo)**

Benefícios:
  - Autenticação de serviço
  - Tráfego interno criptografado
  - Prevenção contra falsidade ideológica/representação ( _impersonation_ )

### 8. Protótipo de Autenticação em Java

- Entidade Usuário [User Entity](./User.java)
- Segurança de Senha BCrypt [Password Service](./PasswordService.java)
- Serviço JWT [JWT Service](./JwtService.java)
- Serviço de Autenticação [Auth Service](./AuthService.java)
- Teste Principal de Autenticação [Main Auth Test](./Main.java)

### 9. Riscos do OWASP Top 10 e Controles Nativos

9.1. Quebra de Controle de Acesso ( _Broken Access Control_ )
  - **Risco:** Usuários acessam recursos não autorizados.
  - **Exemplo:** Cliente acessando _endpoints_ de administrador.
  - **Controles Defensivos:**
    - **RBAC:** Funções ADMIN, CUSTOMER, ORGANIZER, SUPPORT.
    - **Validação de Claims JWT:** role=ADMIN exigida para APIs de
          administração.
    - **Aplicação via API Gateway:** Rotas protegidas centralmente.
    - **Princípio do Menor Privilégio:** Cada serviço acessa apenas os recursos
          necessários.

9.2. Configuração Incorreta de Segurança ( _Security Misconfiguration_ )
  - **Riscos:** Senhas padrão, erros verbosos, portas abertas, buckets de nuvem
       públicos.
  - **Controles Defensivos:**
    - **Contêineres Fortalecidos (** **_Hardened Containers_** **):** Contêineres não-root,
          imagens mínimas.
    - **Cabeçalhos Seguros (** **_Secure Headers_** **):** Content-Security-Policy, X-
          Frame-Options, Strict-Transport-Security.
    - **Gerenciamento de Segredos:** Nunca codificar segredos fixos ( _hardcode_ ).
          Use: Vault, AWS Secrets Manager, Kubernetes Secrets.
    - **Modo de Depuração Desativado:** Ambientes de produção ocultam
          rastreamentos de pilha ( _stack traces_ ).

9.3. Falhas na Cadeia de Suprimentos de Software ( _Software Supply Chain Failures_ )
  - **Riscos:** Bibliotecas ou dependências comprometidas.
  - **Controles Defensivos:**
    - **Verificação de Dependências (Ferramentas):** OWASP Dependency
          Check, Snyk, Dependabot.
    - **Artefatos Assinados:** Verificar a integridade dos pacotes de construção
          ( _build_ ).
    - **SBOM (Lista de Materiais de Software):** Rastrear cada versão de
    dependência.
    - **Apenas Registros Confiáveis:** Nenhum pacote não oficial.

9.4. Falhas Criptográficas ( _Cryptographic Failures_ )
  - **Riscos:** Criptografia fraca expõe os dados do usuário.
  - **Controles Defensivos:**
    - **Hashing Forte de Senhas:** BCrypt, Argon2.
    - **TLS 1.3:** Toda a comunicação criptografada.
    - **Criptografia AES-256:** Dados de pagamento sensíveis criptografados em
repouso.
    - **Rotação de Chaves:** Segredos rotacionados periodicamente.

9.5. Injeção ( _Injection_ )
  - **Riscos:** Injeção SQL, Injeção NoSQL, Injeção de Comando.
  - **Exemplo Vulnerável:** String sql = "SELECT * FROM users WHERE email='" +
email + "'";
  - **Exemplo Seguro:** PreparedStatement stmt =
connection.prepareStatement("SELECT * FROM users WHERE email=?");
stmt.setString(1, email);
  - **Controles Defensivos:**
    - **Prepared Statements:** Previne injeção SQL
    - **Frameworks ORM:** Reduz SQL manual
    - **Validação de Entrada:** Bloqueia _payloads_ malformados
    - **WAF:** Detecta assinaturas de ataque

9.6. Design Inseguro ( _Insecure Design_ )
  - **Riscos:** Segurança não considerada durante o design da arquitetura.
  - **Controles Defensivos:**
    - **Segurança desde a Concepção (** **_Security by Design_** **):** Implementada a
partir da primeira _sprint_.
    - **Modelagem de Ameaças (** **_Threat Modeling_** **) (Antes da
implementação):** STRIDE, DREAD, Árvores de Ataque ( _Attack Trees_ ).
    - **Confiança Zero (** **_Zero Trust_** **):** Cada solicitação é validada.
    - **Transações SAGA Seguras:** O _rollback_ previne compras inconsistentes.

9.7. Falhas de Identificação e Autenticação
  - **Riscos:** Senhas fracas, Preenchimento de credenciais ( _Credential stuffing_ ),
Sequestro de sessão ( _Session hijacking_ ).
  - **Controles Defensivos:**
    - **MFA (Autenticação Multifator):** Recomendado para Admins e
Organizadores.
    - **Política de Senha Forte:** Mínimo de 12 caracteres, Maiúsculas,
Minúsculas, Números, Caracteres especiais.
    - **Bloqueio de Conta (** **_Account Lockout_** **):** 5 tentativas falhas -> bloqueio
temporário.
    - **Expiração Curta de JWT:** Limita o abuso de sessão.
    - **Rotação de Token de Atualização (** **_Refresh Token_** **):** Previne ataques de
repetição.

9.8. Componentes Vulneráveis e Desatualizados
  - **Riscos:** Bibliotecas antigas com CVEs conhecidos.
  - **Controles Defensivos:**
    - **Atualizações de Segurança Automatizadas:** CI/CD valida
vulnerabilidades.
    - **Contêineres Imutáveis:** Toda implantação é reprodutível.
    - **Correção Contínua (** **_Continuous Patching_** **):** Ciclo mensal de _patch_.

9.9. Falhas de Integridade de Software e Dados
  - **Riscos:** Código malicioso injetado no pipeline de CI/CD.
  - **Controles Defensivos:**
    - **Imagens Docker Assinadas:** Verificar a autenticidade da implantação.
    - **Validação de Integridade do CI/CD:** Apenas pipelines confiáveis
realizam a implantação.
    - **Proteção de Ramificação do Git (** **_Git Branch Protection_** **):** Revisão de
código obrigatória.
    - **Verificação de Integridade de Artefatos:** Validação de _checksum_ SHA-
    256.

9.10. SSRF — Falsificação de Solicitação do Lado do Servidor
  - **Riscos:** O invasor força o servidor a acessar recursos internos.
  - **Exemplo Vulnerável:** URL url = new URL(userInputUrl);
  - **Controles Defensivos:**
    - **Lista de Permissão (** **_Allowlist_** **) de URL:** Apenas domínios confiáveis são
aceitos.
    - **Validação de DNS:** Prevenir resolução de IP interno.
    - **Segmentação de Rede:** Serviços internos isolados.
    - **Regras de Firewall de Saída:** Restringir solicitações do servidor.

### 10. Segurança do API Gateway

O API Gateway se torna a camada de segurança central.

|**Funcionalidade** | **Propósito** |
|---|---|
|Validação de JWT | Autenticação |
|Limitação de Taxa ( _Rate Limiting_ ) | Mitigação de DDOS |
|WAF | Proteção contra injeção |
|Filtragem de IP | Bloqueio de fontes maliciosas |
|Registro de Solicitações ( _Logging_ ) | Trilha de auditoria |
|Terminação TLS | Transporte seguro |

### 11. Monitoramento de Segurança

**Monitoramento em Tempo Real usando:** Kafka Streams, Prometheus, Grafana, ELK
Stack. 

**Eventos Detectáveis:**
- Tentativas de força bruta
- Abuso de token
- Pagamentos suspeitos
- Ataques de alta latência
- Tentativas de SSRF

### 12. Pipeline de CI/CD Seguro

Commit do Desenvolvedor -> Análise Estática de Código -> Verificação de
Dependências -> Verificação de Contêiner -> Testes Unitários -> Testes de Segurança -> Artefato Assinado -> Implantação.

### 13. Tecnologias Recomendadas

|**Área** | **Tecnologia**|
|---|---|
|Autenticação | OAuth 2 |
|Autorização | JWT |
|Segurança de Senha | BCrypt |
|Gateway | Spring Cloud Gateway |
|WAF | ModSecurity |
|Segredos | Vault |
|Monitoramento | Grafana |
|Verificação de Dependência | OWASP Dependency Check |
|Segurança de Contêineres | Trivy |
|Provedor de Identidade | Keycloak |

### 14. Design de Banco de Dados Seguro

- **Separação por Serviço:** BD de Usuários, BD de Catálogo, BD de Pagamentos
(Limita o raio de explosão / _blast radius_ ).
- **Criptografia em Repouso:** Dados sensíveis criptografados no banco de dados.
- **Princípio da Exposição Mínima:** Serviços de pagamento nunca expõem os
dados completos do cartão.


### 15. Arquitetura Segura Final

Cliente -> HTTPS + OAuth2 -> API Gateway -> Validação JWT + WAF -> Microsserviços -> Kafka + mTLS -> Bancos de Dados Criptografados.

### 16. Estratégia de Teste de Segurança

|**Tipo de Teste** | **Objetivo** |
|---|---|
|Testes Unitários | Validar regras de negócio |
|Testes de Integração | Validar APIs |
|Testes de Penetração | Simular ataques |
|SAST | Análise estática |
|DAST | Análise dinâmica |
|Verificação de Dependências | Detectar bibliotecas vulneráveis |

### 17. Conclusão

A Fase 3 transforma o EventMaster em uma plataforma distribuída segura alinhada com os modernos padrões de segurança corporativa.

A arquitetura agora inclui:
  - Autenticação OAuth 2.
  - Autorização JWT
  - Controle de acesso RBAC
  - Mitigações do OWASP Top 10
  - CI/CD seguro
  - Comunicações criptografadas
  - Microsserviços seguros
  - Validação de Confiança Zero ( _Zero Trust_ )
  - Monitoramento de ameaças em tempo real

A implementação proposta em Java demonstra:
  - Hashing seguro de senhas
  - Geração de JWT
  - Fluxo de autenticação
  - Práticas de codificação segura
  - Padrões de arquitetura defensiva

A plataforma resultante torna-se:
  - Escalável
  - Resiliente
  - Observável
  - Segura desde a concepção ( _Secure-by-design_ )
  - Nativa da nuvem ( _Cloud-native_ )
  - Pronta para produção em sistemas de bilhetagem de grande escala
