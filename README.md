![CI](https://github.com/PedroAMDC/pedroaugustomartinsdecarvalho039544/actions/workflows/ci.yml/badge.svg)

# Sistema de Gerenciamento de Artistas e Albuns

## Dados de Inscrição

- **Candidato:** Pedro Augusto Martins de Carvalho
- **CPF (6 primeiros dígitos):** 039544
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

### Pré-requisitos
- Docker e Docker Compose instalados
- Git

### Execução com Docker Compose

```bash
# Clonar o repositório
git clone https://github.com/PedroAMDC/pedroaugustomartinsdecarvalho039544.git
cd pedroaugustomartinsdecarvalho039544

# Copiar variáveis de ambiente
cp .env.example .env

# Executar todos os serviços
docker-compose up --build
```

### URLs dos Serviços

| Serviço | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/q/swagger-ui |
| Health Check | http://localhost:8080/q/health |
| MinIO Console | http://localhost:9001 |

### Credenciais Padrão

**MinIO:**
- Usuário: `minioadmin`
- Senha: `minioadmin`

**Banco de Dados:**
- Host: `localhost:5432`
- Database: `artistasdb`
- Usuário: `postgres`
- Senha: `postgres`

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

## Licença

Este projeto foi desenvolvido como parte do Processo Seletivo Simplificado nº 001/2026/SEPLAG do Estado de Mato Grosso.

---

**Desenvolvido por:** Pedro Augusto Martins de Carvalho
