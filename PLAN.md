# PLAN — Arquitetura do Sistema

## 1. Estrutura de Diretórios

```
C:\ProjetoTreinoIa\
├── backend\
│   └── src\main\java\com\taskflow\
│       ├── config\           → DataInitializer, GlobalExceptionHandler, WebConfig
│       ├── controller\       → TarefaController, UsuarioController, AuthController, CategoriaController
│       ├── dto\              → TarefaDTO, UsuarioDTO, CategoriaDTO, LoginRequest/Response
│       ├── model\            → Tarefa, Usuario, Categoria, TarefaStatus, UsuarioRole
│       ├── repository\       → TarefaRepository, UsuarioRepository, CategoriaRepository
│       └── service\          → TarefaService, UsuarioService, AuthService, CategoriaService
├── frontend\
│   └── src\app\
│       ├── components\
│       │   ├── login\        → LoginComponent (standalone)
│       │   ├── task-list\    → TaskListComponent (grid, buscas, bulk)
│       │   ├── task-form\    → TaskFormComponent (criar/editar)
│       │   ├── categoria-list\ → CategoriaListComponent (CRUD inline)
│       │   ├── usuario-list\ → UsuarioListComponent (tabela)
│       │   └── usuario-form\ → UsuarioFormComponent (criar/editar)
│       ├── services\         → task.service, usuario.service, categoria.service, auth.service
│       ├── models\           → task.ts, usuario.ts, categoria.ts
│       ├── guards\           → auth.guard
│       ├── app.ts            → AppComponent (topbar + router-outlet)
│       ├── app.routes.ts     → Definição de rotas
│       └── app.config.ts     → Providers (HttpClient, Router, etc)
├── SPEC.md
├── PLAN.md
├── HOOKS.md
└── bugs resolvidos.txt
```

---

## 2. Fluxo de Comunicação

```
[Browser] ←→ [Angular 21 (Vite, :4200)] ←HTTP JSON→ [Spring Boot 3.2.5 (:8080)] ←JDBC→ [H2 :memory]
                                                              │
                                                       GlobalExceptionHandler
                                                              │
                                                    Tarefa/Usuario/CategoriaService
                                                              │
                                                    JpaRepository (Spring Data)
```

---

## 3. Padrões e Convenções

### 3.1. Autenticação (auth.guard.ts)
- Verifica `localStorage.getItem('usuarioLogado')`
- Se `null`: redireciona para `/login`
- Se existe e rota atual é `/login`: redireciona para `/`
- No `subscribe`: usa `window.location.href` em vez de `router.navigate()`

### 3.2. Headers (services)
- Método `getHeaders(): Record<string, string>` em cada service
- Header `X-User-Id` = `usuarioLogado.id` do localStorage
- Content-Type `application/json`
- **Não** usa `HttpHeaders` do Angular — usa `Record<string, string>`

### 3.3. Permissão (TarefaService.java)
- `verificarPermissao(...)`: lança `RuntimeException("Acesso negado: role ... necessaria")` se sem permissão
- Capturado por `GlobalExceptionHandler` → 403
- Hierarquia de roles via `enum.compareTo()`: ADMIN > DISTRIBUIDOR > EXECUTOR

### 3.4. Componentes Standalone
- Todos os componentes declarados como `standalone: true`
- Imports diretos no componente (sem NgModule)
- Template e estilos inline ou externos (maioria externos)

### 3.5. CSS
- `text-transform: uppercase` em inputs de texto
- Grid: `display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px;` (task-list)
- Responsivo: `@media (max-width: 1200px) { 3 columns }`, `@media (max-width: 900px) { 2 columns }`, `@media (max-width: 600px) { 1 column }`
- Task card: borda esquerda colorida (vermelha ≤3d, amarela 4-7d)
- Cards selecionados: `outline: 2px solid #1976d2`

---

## 4. Configuração de Desenvolvimento

| Propriedade | Valor |
|-------------|-------|
| Servidor | `localhost:8080` |
| H2 Console | `/h2-console` |
| H2 JDBC | `jdbc:h2:mem:taskflow` |
| H2 User/Pass | `sa` / (vazio) |
| JPA DDL | `create-drop` |
| CORS | `http://localhost:4200` |
| Frontend dev | `ng serve` (:4200) |
| Frontend build | `ng build` |

---

## 5. Budget CSS

O Angular verifica tamanho de estilos por componente. O `task-list.css` (~4.88 kB) excede o limite padrão (4.00 kB), gerando warning durante o build mas sem impedir a compilação.

---

## 6. Dependências Principais

**Backend** (pom.xml): spring-boot-starter-web, spring-boot-starter-data-jpa, h2, validation-api, spring-boot-starter-test, junit-jupiter

**Frontend** (package.json): @angular/core@21, @angular/router@21, @angular/common@21, zone.js, typescript, vite
