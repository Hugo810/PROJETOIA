# SPEC — TaskFlow

## 1. Visão Geral

Sistema multi-usuário de gerenciamento de tarefas com três papéis (ADMIN, DISTRIBUIDOR, EXECUTOR), ciclo de vida de tarefas em três status (PENDENTE → EM_EXECUCAO → CONCLUIDA) e autenticação por header customizado.

| Item | Valor |
|------|-------|
| **Propósito** | Gerenciamento de tarefas com distribuição e execução |
| **Frontend** | Angular 21 standalone + Vite |
| **Backend** | Spring Boot 3.2.5 + Java 19 |
| **BD** | H2 em memória (dev) / Oracle (prod via profile) |
| **Auth** | Header `X-User-Id` + localStorage (sem JWT) |

---

## 2. Modelo de Dados

### 2.1 Entidades

```
USUARIO
├── id: Long (PK, auto)
├── nome: String(100)
├── email: String(100) [UNIQUE]
├── senha: String(100)
└── role: UsuarioRole [ENUM: ADMIN | DISTRIBUIDOR | EXECUTOR]

CATEGORIA
├── id: Long (PK, auto)
└── nome: String(50) [UNIQUE]

TAREFA
├── id: Long (PK, auto)
├── titulo: String(200) [NOT NULL]
├── descricao: String(1000)
├── categoria_id: Long (FK → CATEGORIA) [NOT NULL]
├── prazo: LocalDate [NOT NULL, FUTURE_OR_PRESENT]
├── status: TarefaStatus [ENUM: PENDENTE | EM_EXECUCAO | CONCLUIDA]
├── data_criacao: LocalDateTime [NOT NULL, updatable=false]
├── data_conclusao: LocalDateTime
├── responsavel_id: Long (FK → USUARIO)
└── distribuidor_id: Long
```

### 2.2 Enums

**UsuarioRole** (hierarquia): `ADMIN > DISTRIBUIDOR > EXECUTOR`
**TarefaStatus** (ciclo): `PENDENTE → EM_EXECUCAO → CONCLUIDA`

---

## 3. API Endpoints

### 3.1 Auth
| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| POST | `/api/auth/login` | — | Login (email + senha) → UsuarioDTO |

### 3.2 Tarefas
| Método | Rota | Header | Role | Descrição |
|--------|------|--------|------|-----------|
| GET | `/api/tarefas` | — | — | Listar (filtros: status, categoriaId, responsavelId; paginação) |
| GET | `/api/tarefas/{id}` | — | — | Buscar por ID |
| POST | `/api/tarefas` | X-User-Id | DISTRIBUIDOR | Criar tarefa |
| PUT | `/api/tarefas/{id}` | X-User-Id | DISTRIBUIDOR | Atualizar tarefa |
| PATCH | `/api/tarefas/{id}/iniciar` | X-User-Id | Responsavel ou ADM/DIST | Iniciar execução |
| PATCH | `/api/tarefas/{id}/concluir` | X-User-Id | Responsavel ou ADM/DIST | Concluir tarefa |
| PATCH | `/api/tarefas/{id}/distribuir` | X-User-Id | DISTRIBUIDOR | Atribuir responsável |
| DELETE | `/api/tarefas/{id}` | X-User-Id | ADMIN | Excluir tarefa |

### 3.3 Categorias
| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| GET | `/api/categorias` | — | Listar todas |
| GET | `/api/categorias/{id}` | — | Buscar por ID |
| POST | `/api/categorias` | — | Criar |
| PUT | `/api/categorias/{id}` | — | Atualizar |
| DELETE | `/api/categorias/{id}` | — | Excluir |

### 3.4 Usuários
| Método | Rota | Header | Role | Descrição |
|--------|------|--------|------|-----------|
| GET | `/api/usuarios` | X-User-Id | Qualquer autenticado | Listar |
| GET | `/api/usuarios/{id}` | — | — | Buscar |
| POST | `/api/usuarios` | X-User-Id | ADMIN | Criar |
| PUT | `/api/usuarios/{id}` | X-User-Id | ADMIN | Atualizar |
| DELETE | `/api/usuarios/{id}` | X-User-Id | ADMIN | Excluir |

---

## 4. Regras de Permissão

| Ação | ADMIN | DISTRIBUIDOR | EXECUTOR |
|------|-------|-------------|----------|
| Login | ✔ | ✔ | ✔ |
| Listar usuários | ✔ | ✔ | ✔ |
| Criar/Editar/Excluir usuário | ✔ | ✘ (403) | ✘ (403) |
| Criar/Editar tarefa | ✔ | ✔ | ✘ (403) |
| Excluir tarefa | ✔ | ✘ (403) | ✘ (403) |
| Distribuir tarefa | ✔ | ✔ | ✘ |
| Iniciar tarefa própria | ✔ | ✔ | ✔ |
| Iniciar qq tarefa | ✔ | ✔ | ✘ |
| Concluir tarefa própria | ✔ | ✔ | ✔ |
| Concluir qq tarefa | ✔ | ✔ | ✘ |

Erro de permissão → HTTP 403 `{"erro": "Acesso negado: role DISTRIBUIDOR necessaria"}`
Email duplicado → HTTP 400 `{"erro": "Email ja cadastrado."}`

---

## 5. Frontend — Rotas

| Rota | Componente | Descrição |
|------|-----------|-----------|
| `/login` | `Login` | Tela de login |
| `/` | `TaskList` | Grid de tarefas (4 colunas) |
| `/nova` | `TaskForm` | Criar tarefa |
| `/editar/:id` | `TaskForm` | Editar tarefa |
| `/categorias` | `CategoriaList` | Gerenciar categorias |
| `/usuarios` | `UsuarioList` | Listar usuários |
| `/usuarios/novo` | `UsuarioForm` | Criar usuário |
| `/usuarios/editar/:id` | `UsuarioForm` | Editar usuário |

---

## 6. Regras de Negócio

1. **Título e descrição**: convertidos para maiúsculas no frontend antes do submit (`text-transform: uppercase` visual + `.toUpperCase()` no JS)
2. **Prazo**: deve ser hoje ou futuro (validação backend `@FutureOrPresent` + frontend `type="date"`)
3. **Categoria**: ligada por chave estrangeira (`categoria_id`), obrigatória
4. **Status flow**: PENDENTE → (iniciar) → EM_EXECUCAO → (concluir) → CONCLUIDA; pular etapas não é bloqueado pelo backend
5. **Data conclusão**: setada automaticamente ao concluir
6. **Data criação**: setada automaticamente via `@PrePersist`
7. **Distribuidor**: quem criou ou distribuiu a tarefa fica registrado em `distribuidor_id`
8. **Responsável**: pode ser alterado via distribuir a qualquer momento
9. **Urgência visual** (frontend): ≤3 dias = borda vermelha, 4-7 dias = borda amarela, prazo vencido = texto vermelho
10. **Busca**: filtro local por título (client-side)
11. **Ações em massa**: selecionar tarefas por checkbox → concluir ou excluir em lote

---

## 7. Dados de Teste (DataInitializer)

**Usuários**: admin@taskflow.com/admin (ADMIN), dist@taskflow.com/123456 (DISTRIBUIDOR), executor@taskflow.com/123456 (EXECUTOR)

**Categorias**: TRABALHO, ESTUDOS, PESSOAL, URGENTE, SAUDE, FINANCEIRO

**Tarefas**: 12 tarefas pré-criadas com diversos status, categorias e responsáveis.

---

## 8. Tratamento de Erros

| Situação | HTTP | Body |
|----------|------|------|
| Acesso negado | 403 | `{"erro": "..."}` |
| Violação unique (email) | 400 | `{"erro": "Email ja cadastrado."}` |
| Erro de validação (bean) | 400 | `{"erro": "campo: mensagem; ..."}` |
| Recurso não encontrado | 400 | `{"erro": "Tarefa nao encontrada: {id}"}` |
| Erro genérico | 400 | `{"erro": ex.getMessage()}` |
