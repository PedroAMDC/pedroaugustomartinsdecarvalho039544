![CI](https://github.com/PedroAMDC/music-catalog/actions/workflows/ci.yml/badge.svg)

# Sistema de Gerenciamento de Artistas e Albuns

## Dados de Inscrição

- **Author: PedroAMDC
- **
- **Vaga:** Analista de Tecnologia da Informação - Perfil Engenheiro da Computação (Sênior)
- **Projeto:** Full Stack (60 pontos)

---

## Stack Tecnológico

### Backend
- **Linguagem:** Java 17+
- **Framework:** Quarkus
- **Build Tool:** Maven
- **Banco de Dados:** PostgreSQL 16
- **Storage:** MinIO (S3 Compatible)
- **Autenticação:** JWT (SmallRye JWT)
- **Documentação:** OpenAPI/Swagger
- **Migrations:** Flyway

### Frontend
- **Framework:** React 19 + Next.js 16
- **Linguagem:** TypeScript
- **Estilização:** Tailwind CSS + shadcn/ui
- **State Management:** React Context + Hooks (Facade Pattern)

### Infraestrutura
- **Containerização:** Docker + Docker Compose
- **CI/CD:** GitHub Actions

---

## Como Executar

### Pre-requisitos

| Requisito | Versao minima | Verificar instalacao |
|-----------|---------------|----------------------|
| Docker | 20.10+ | `docker --version` |
| Docker Compose | v2.0+ | `docker compose version` |
| Git | 2.30+ | `git --version` |

**Recursos recomendados:** 4 GB de RAM disponivel para os containers.

**Portas necessarias (devem estar livres):**

| Porta | Servico |
|-------|---------|
| 3000 | Frontend (Next.js) |
| 5432 | PostgreSQL |
| 8080 | Backend (Quarkus) |
| 9000 | MinIO (API) |
| 9001 | MinIO (Console) |

### Clone e Setup

**1. Clonar o repositorio:**

```bash
git clone https://github.com/PedroAMDC/music-catalog.git
cd music-catalog
```

**2. Configurar variaveis de ambiente:**

```bash
cp .env.example .env
```

O arquivo `.env.example` contem valores padrao prontos para uso local. Edite o `.env` apenas se precisar personalizar alguma configuracao (ver tabela de variaveis abaixo).

**3. Configurar chaves RSA para JWT:**

As chaves RSA sao necessarias para autenticacao JWT. Escolha uma das opcoes:

**Opcao A - Copiar chaves de exemplo (recomendado para avaliacao rapida):**

```bash
cd backend/src/main/resources
cp privateKey.example.pem privateKey.pem
cp publicKey.example.pem publicKey.pem
cd ../../../..
```

**Opcao B - Gerar chaves novas (recomendado para producao):**

```bash
cd backend/src/main/resources
openssl genrsa -out privateKey.pem 2048
openssl rsa -in privateKey.pem -pubout -out publicKey.pem
cd ../../../..
```

> **Nota:** As chaves RSA (.pem) estao no .gitignore por seguranca.

**4. Iniciar todos os servicos:**

```bash
docker compose up --build
```

Aguarde ate que todos os containers estejam saudaveis. O backend pode levar alguns segundos para iniciar apos o PostgreSQL e MinIO estarem prontos. A inicializacao esta completa quando voce vir no log:

```
artistas-backend  | Quarkus started in ...
artistas-frontend | Ready in ...
```

### Variaveis de Ambiente

Todas as variaveis estao documentadas no arquivo `.env.example`. A tabela abaixo descreve cada uma:

**PostgreSQL:**

| Variavel | Descricao | Valor padrao |
|----------|-----------|--------------|
| `POSTGRES_HOST` | Host do banco de dados | `postgres` |
| `POSTGRES_PORT` | Porta do banco de dados | `5432` |
| `POSTGRES_DB` | Nome do banco de dados | `artistas_albuns` |
| `POSTGRES_USER` | Usuario do banco | `postgres` |
| `POSTGRES_PASSWORD` | Senha do banco | `postgres123` |

**MinIO (Object Storage):**

| Variavel | Descricao | Valor padrao |
|----------|-----------|--------------|
| `MINIO_ENDPOINT` | URL do servico MinIO | `http://minio:9000` |
| `MINIO_ROOT_USER` | Usuario root do MinIO | `minioadmin` |
| `MINIO_ROOT_PASSWORD` | Senha root do MinIO | `minioadmin123` |
| `MINIO_BUCKET` | Nome do bucket para capas | `albuns-capas` |
| `MINIO_PRESIGNED_URL_EXPIRY` | Expiracao da URL pre-assinada (segundos) | `1800` |

**Backend (Quarkus):**

| Variavel | Descricao | Valor padrao |
|----------|-----------|--------------|
| `QUARKUS_HTTP_PORT` | Porta do servidor | `8080` |
| `QUARKUS_PROFILE` | Perfil de execucao | `dev` |
| `JWT_SECRET` | Segredo para assinatura JWT | (valor longo no .env.example) |
| `JWT_ISSUER` | Emissor do token JWT | `artistas-albuns-api` |
| `JWT_EXPIRATION_SECONDS` | Expiracao do access token | `300` (5 min) |
| `JWT_REFRESH_EXPIRATION_SECONDS` | Expiracao do refresh token | `86400` (24h) |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas para CORS | `http://localhost:3000` |
| `RATE_LIMIT_REQUESTS_PER_MINUTE` | Limite de requisicoes por minuto | `10` |

**Frontend (Next.js):**

| Variavel | Descricao | Valor padrao |
|----------|-----------|--------------|
| `NEXT_PUBLIC_API_URL` | URL da API backend | `http://localhost:8080` |
| `NEXT_PUBLIC_WS_URL` | URL do WebSocket | `ws://localhost:8080` |

**API Externa:**

| Variavel | Descricao | Valor padrao |
|----------|-----------|--------------|
| `EXTERNAL_API_URL` | URL da API publica de regionais | `https://api-publica-mt.seplag.mt.gov.br` |

### Comandos Docker Compose

```bash
# Iniciar todos os servicos (primeiro uso ou apos mudancas)
docker compose up --build

# Iniciar em segundo plano (detached)
docker compose up --build -d

# Parar todos os servicos
docker compose down

# Parar e remover volumes (reset completo do banco e MinIO)
docker compose down -v

# Ver logs de todos os servicos
docker compose logs -f

# Ver logs de um servico especifico
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres
docker compose logs -f minio

# Reiniciar um servico especifico
docker compose restart backend

# Reconstruir e reiniciar apenas um servico
docker compose up --build -d backend

# Verificar status dos containers
docker compose ps
```

### URLs dos Servicos

| Servico | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/q/swagger-ui |
| Health Check | http://localhost:8080/q/health |
| MinIO Console | http://localhost:9001 |

### Credenciais Padrao

**Aplicacao (registrar novo usuario via frontend ou API):**

```bash
curl -X POST http://localhost:8080/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@teste.com","password":"Senha@123","confirmPassword":"Senha@123","nome":"Admin"}'
```

**MinIO Console (http://localhost:9001):**
- Usuario: `minioadmin`
- Senha: `minioadmin123`

**PostgreSQL (localhost:5432):**
- Database: `artistas_albuns`
- Usuario: `postgres`
- Senha: `postgres123`

### Troubleshooting

**Porta ja em uso:**

```
Error: bind: address already in use
```

Verifique quais processos estao usando as portas necessarias:

```bash
# Linux/macOS
lsof -i :3000 -i :5432 -i :8080 -i :9000 -i :9001

# Windows (PowerShell)
netstat -ano | findstr "3000 5432 8080 9000 9001"
```

Encerre o processo conflitante ou altere as portas no `docker-compose.yml`.

**Backend nao conecta no PostgreSQL:**

```
Connection refused / FATAL: database "artistas_albuns" does not exist
```

O PostgreSQL pode nao ter terminado a inicializacao. Aguarde e tente novamente. Se persistir, reinicie com volumes limpos:

```bash
docker compose down -v
docker compose up --build
```

**Erro de chave RSA / JWT:**

```
java.security.spec.InvalidKeySpecException / Could not read private key
```

As chaves RSA nao foram configuradas. Siga o passo 3 da secao "Clone e Setup" para copiar ou gerar as chaves.

**MinIO bucket nao encontrado:**

```
The specified bucket does not exist
```

O container `minio-setup` cria o bucket automaticamente. Verifique se executou com sucesso:

```bash
docker compose logs minio-setup
```

Se necessario, crie manualmente via MinIO Console (http://localhost:9001) com o nome `albuns-capas`.

**Frontend nao conecta na API:**

```
ECONNREFUSED / Network Error
```

Verifique se o backend esta rodando e saudavel:

```bash
curl http://localhost:8080/q/health
```

Se estiver em Windows com Docker Desktop, certifique-se de que `localhost` esta acessivel dentro do container.

**Docker sem memoria:**

```
Exited (137) / OOMKilled
```

Aumente a memoria do Docker Desktop (Settings > Resources) para no minimo 4 GB.

**Rebuild completo (quando nada funciona):**

```bash
docker compose down -v
docker system prune -f
docker compose up --build
```

---

## Arquitetura

### Backend (Quarkus)

```
backend/
├── src/main/java/com/projeto/
│   ├── api/v1/          # REST Endpoints
│   ├── config/          # Configurações (CORS, Security)
│   ├── filters/         # Rate Limit Filter
│   ├── models/          # JPA Entities
│   ├── schemas/         # DTOs
│   ├── services/        # Business Logic
│   └── websocket/       # WebSocket Handlers
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/    # Flyway Migrations
└── src/test/            # Testes
```

### Frontend (Next.js)

```
frontend/
├── app/                 # Next.js App Router
│   ├── (auth)/          # Rotas de autenticação
│   ├── artistas/        # Páginas de artistas
│   └── albuns/          # Páginas de álbuns
├── components/          # Componentes React
│   ├── ui/              # shadcn/ui components
│   └── common/          # Componentes reutilizáveis
├── lib/                 # Services (Facade Pattern)
├── hooks/               # Custom Hooks
├── contexts/            # React Contexts
└── types/               # TypeScript Types
```

### Diagrama do Sistema

```
+-------------------+
|     Browser       |
+--------+----------+
         |
         | HTTP :3000
         v
+-------------------+     WebSocket
|    Frontend       |------------------+
|   Next.js 16      |                  |
+--------+----------+                  |
         |                             |
         | HTTP/REST :8080             |
         v                             v
+-------------------+     +-------------------+
|    Backend        |     |    WebSocket      |
|   Quarkus 3.17    |     | /ws/notifications |
+--------+----------+     +-------------------+
         |
    +----+----+
    |         |
    v         v
+-------+  +-------+
|  DB   |  | MinIO |
| PgSQL |  |  S3   |
| :5432 |  | :9000 |
+-------+  +-------+
```

### Modelo de Dados (ER)

```
+---------------+       +------------------+       +---------------+
|   Usuario     |       |  artista_album   |       |    Artista    |
+---------------+       +------------------+       +---------------+
| id (PK)       |       | artista_id (FK)  |------>| id (PK)       |
| email (UQ)    |       | album_id (FK)    |       | nome          |
| password_hash |       +------------------+       | tipo          |
| nome          |               |                  | created_at    |
| ativo         |               |                  +---------------+
| created_at    |               |                         ^
+---------------+               v                         |
                        +---------------+                 |
                        |    Album      |-----------------+
                        +---------------+        N:M
                        | id (PK)       |
                        | titulo        |
                        | ano_lancamento|
                        | created_at    |
                        +-------+-------+
                                |
                                | 1:N
                                v
                        +---------------+
                        |  CapaAlbum    |
                        +---------------+
                        | id (PK)       |
                        | album_id (FK) |
                        | minio_key     |
                        | original_name |
                        | content_type  |
                        | tamanho_bytes |
                        +---------------+
```

### Fluxo de Autenticacao (JWT)

```
+--------+          +----------+          +---------+
| Client |          | Backend  |          |   DB    |
+---+----+          +----+-----+          +----+----+
    |                    |                     |
    | POST /v1/auth/login                      |
    |------------------->|                     |
    |                    | Busca usuario       |
    |                    |-------------------->|
    |                    |<--------------------|
    |                    |                     |
    |                    | Valida senha (BCrypt)
    |                    | Gera JWT (5min)     |
    |                    | Gera Refresh (24h)  |
    |<-------------------|                     |
    | { accessToken, refreshToken }            |
    |                    |                     |
    | GET /v1/artistas   |                     |
    | Authorization: Bearer <token>            |
    |------------------->|                     |
    |                    | Valida JWT          |
    |                    | Rate Limit Check    |
    |                    |-------------------->|
    |<-------------------|                     |
    | { artistas[] }     |                     |
    |                    |                     |
    | PUT /v1/auth/refresh                     |
    | { refreshToken }   |                     |
    |------------------->|                     |
    |                    | Valida Refresh      |
    |                    | Gera novo par       |
    |<-------------------|                     |
    | { accessToken, refreshToken }            |
+---+----+          +----+-----+          +----+----+
```

---

## Funcionalidades Implementadas

### Requisitos Gerais
- [x] CORS configurado
- [x] Autenticação JWT (expiração 5 min + renovação)
- [x] Endpoints POST, PUT, GET
- [x] Paginação na consulta de álbuns
- [x] Consultas parametrizadas (cantores/bandas)
- [x] Ordenação alfabética (asc/desc)
- [x] Upload de imagens de capa
- [x] Armazenamento no MinIO
- [x] Links pré-assinados (30 min expiração)
- [x] Versionamento de endpoints (/v1/)
- [x] Flyway Migrations
- [x] Documentação OpenAPI/Swagger

### Requisitos Sênior
- [x] Health Checks (Liveness/Readiness)
- [x] Testes unitários
- [x] WebSocket para notificações
- [x] Rate limit (10 req/min por usuário)
- [x] Sincronização de regionais

### Frontend
- [x] Layout responsivo
- [x] Tailwind CSS
- [x] Lazy Loading Routes
- [x] Paginação
- [x] TypeScript
- [x] Padrão Facade
- [x] Gerenciamento de estado
- [x] Testes unitários básicos

---

## Dados de Exemplo (Seed)

O banco é populado automaticamente com os seguintes dados:

| Artista | Álbuns |
|---------|--------|
| Serj Tankian | Harakiri, Black Blooms, The Rough Dog |
| Mike Shinoda | The Rising Tied, Post Traumatic, Post Traumatic EP, Where'd You Go |
| Michel Teló | Bem Sertanejo, Bem Sertanejo - O Show (Ao Vivo), Bem Sertanejo - (1ª Temporada) - EP |
| Guns N' Roses | Use Your Illusion I, Use Your Illusion II, Greatest Hits |

---

## Decisões Técnicas

### Por que Quarkus?
- Performance superior em startup e runtime
- Container-first design ideal para Docker
- Excelente suporte a extensões (JWT, Flyway, OpenAPI)
- Developer experience com hot-reload

### Por que Next.js?
- Build otimizado com Turbopack
- App Router com Server Components
- Lazy loading nativo
- Excelente integração com TypeScript

### Por que MinIO?
- API 100% compatível com S3
- Fácil de executar localmente via Docker
- Suporte nativo a presigned URLs

---

## Executar Testes

### Backend
```bash
cd backend
./mvnw test
```

### Frontend
```bash
cd frontend
npm test
```

---

## Commits

Este projeto segue o padrao de **commits semanticos** para manter um historico claro e consistente.

### Formato da Mensagem

```
<tipo>: <descricao>
```

**Regras:**
- Mensagens sempre em **ingles**
- Usar verbo no **imperativo** (add, fix, update, remove)
- Maximo de **72 caracteres** na primeira linha
- Descricao clara e objetiva do que foi alterado

### Tipos de Commit

| Tipo | Descricao | Exemplo |
|------|-----------|---------|
| `feat:` | Nova funcionalidade | `feat: add artist image upload endpoint` |
| `fix:` | Correcao de bug | `fix: resolve album pagination returning wrong count` |
| `chore:` | Tarefas administrativas | `chore: update quarkus dependencies to 3.17` |
| `docs:` | Documentacao | `docs: add commit guidelines to README` |
| `test:` | Adicao ou modificacao de testes | `test: add unit tests for ArtistaService` |
| `refactor:` | Refatoracao sem mudanca de comportamento | `refactor: extract validation logic to separate class` |

### Convencao de Branches

| Padrao | Uso | Exemplo |
|--------|-----|---------|
| `feature/T0XX-description` | Novas funcionalidades | `feature/T015-add-album-search` |
| `fix/T0XX-description` | Correcoes de bugs | `fix/T023-pagination-offset` |

### Git Flow

```
feature/* ou fix/*  -->  develop  -->  main
```

- **develop:** Branch principal de desenvolvimento
- **main:** Branch de producao (releases estaveis)
- Todas as features e fixes devem ser mergeadas via **Pull Request**

---

## Metodologia

Este projeto foi desenvolvido seguindo praticas ageis:

- **Kanban Board:** Backlog organizado em [GitHub Projects](../../projects) com colunas To Do, In Progress e Done
- **Tasks incrementais:** Cada funcionalidade foi quebrada em tasks pequenas e bem definidas com criterios de aceite claros
- **Commits semanticos:** Historico de commits organizado e descritivo
- **Feature branches:** Cada task desenvolvida em branch isolada com merge via Pull Request
- **Testes obrigatorios:** Cobertura de testes unitarios como requisito para conclusao de tasks

---

## Licença

Este projeto foi desenvolvido como parte do Technical Assessment Project do Estado de Mato Grosso.

---

**Desenvolvido por:** PedroAMDC
