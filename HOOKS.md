# HOOKS — Fluxos e Operações

## 1. Fluxo de Autenticação

```
Usuário abre /login
  → LoginComponent.render(): form com email + senha
  → Usuário preenche e submit
    → LoginComponent.onSubmit()
      → AuthService.login(email, senha)
        → POST /api/auth/login {email, senha}
        ← 200 {id, nome, email, role}
      → salva em localStorage.setItem('usuarioLogado', JSON.stringify(response))
      → window.location.href = '/'  (via setTimeout para evitar ExpressionChangedAfterItHasBeenCheckedError)
  → AuthGuard.canActivate(): localStorage.getItem('usuarioLogado') != null
    → OK: renderiza AppComponent com topbar + <router-outlet>
```

---

## 2. Fluxo de Criação de Tarefa

```
Usuário (DISTRIBUIDOR ou ADMIN) clica "Nova Tarefa" (em /)
  → router.navigate(['/nova'])
  → TaskFormComponent.render()
    → Carrega lista de usuários (/api/usuarios)
    → Carrega lista de categorias (/api/categorias)
    → Form titulo (uppercase), descricao (uppercase), dropdown categoria, dropdown responsavel, date prazo
  → Submit
    → titulo.toUpperCase(), descricao.toUpperCase()
    → TaskService.criarTarefa(dto)
      → POST /api/tarefas com header X-User-Id
      ← 200 TarefaDTO
    → router.navigate(['/'])
```

---

## 3. Fluxo de Distribuição (Atribuir Responsável)

```
Usuário DISTRIBUIDOR ou ADMIN:
  Via edição de tarefa:
    → TaskFormComponent em edição
    → Seleciona responsável no dropdown
    → Submit → PUT /api/tarefas/{id} com responsavel_id

  Via card na listagem:
    → Botão "Distribuir" (visível apenas se não for o executor/responsável atual)
    → PATCH /api/tarefas/{id}/distribuir?email=... (ou abre modal — depende do fluxo)

Validações (TarefaService):
  - Distribuidor deve ter role DISTRIBUIDOR ou ADMIN
  - Responsável deve existir
  - Retorna TarefaDTO atualizado
```

---

## 4. Fluxo de Execução

```
[Responsável ou ADMIN/DISTRIBUIDOR]
  Card da tarefa (PENDENTE): botão "Iniciar"
    → PATCH /api/tarefas/{id}/iniciar com X-User-Id
    ← 200 status: EM_EXECUCAO
    → Card atualiza: botão "Concluir" aparece, "Iniciar" some

  Card da tarefa (EM_EXECUCAO): botão "Concluir"
    → PATCH /api/tarefas/{id}/concluir com X-User-Id
    ← 200 status: CONCLUIDA, data_conclusao preenchida
    → Card tachado + cinza

  Card da tarefa (EM_EXECUCAO): checkbox de conclusão (atalho)
    → PATCH /api/tarefas/{id}/concluir (mesma lógica)
    ← 200 status: CONCLUIDA
```

---

## 5. Filtros (Task List)

```
Ao carregar / (TaskListComponent)
  → ngOnInit(): carrega todas tarefas e armazena em listaCompleta
  → Aplica filtro ativo + ordenação
  → Renderiza grid

Filtros:
  "Todas" (padrão)  → listaCompleta sem filtro
  "Minhas tarefas"  → GET /api/tarefas?responsavelId={meuId}
  "Atrasadas"       → listaCompleta.filter(t => new Date(t.prazo) < hoje)
  "Esta semana"     → listaCompleta.filter(t => prazo <= hoje + 7 dias)

Busca:
  → ngModel searchTerm ligado a input
  → listaFiltrada.filter(t => t.titulo.includes(searchTerm))
  → Aplica sobre resultado dos filtros rápidos

Ordenação:
  1. Urgência (prazo ≤ 3 dias) primeiro
  2. Não concluídas primeiro
  3. Prazo ascendente (mais próximo)

Indicador de urgência:
  - prazo ≤ 3 dias: classe .urgencia-alta (borda vermelha #d32f2f)
  - 4-7 dias: classe .urgencia-media (borda amarela #f9a825)
  - prazo vencido: texto do prazo em vermelho (#d32f2f)
```

---

## 6. Ações em Massa (Bulk)

```
Seleção:
  → Checkbox em cada card → [(ngModel)] tarefa.selecionada
  → Checkbox "Selecionar todos" no header do grid
  → Barra de ações em massa aparece apenas com ≥ 1 selecionado
  → Contador "X selecionada(s)"

Concluir selecionadas:
  → Para cada tarefa selecionada com status != CONCLUIDA:
      PATCH /api/tarefas/{id}/concluir
  → Aguarda todas Promises/allSettled
  → Recarrega lista

Excluir selecionadas (apenas ADMIN):
  → Confirma com window.confirm
  → Para cada tarefa selecionada:
      DELETE /api/tarefas/{id}
  → Recarrega lista
  → Se não-ADMIN tenta: botão fica desabilitado
```

---

## 7. Fluxo de Edição de Tarefa

```
TaskFormComponent em modo edição
  → ngOnInit: id = ActivatedRoute.params.id
  → Se id != null: carrega tarefa via GET /api/tarefas/{id}
  → Preenche form com dados atuais
  → Submit:
    → toUpperCase() nos textos
    → PUT /api/tarefas/{id}
    ← 200 TarefaDTO atualizado
  → router.navigate(['/'])
```

---

## 8. Fluxo de Login (Assíncrono)

```
LoginComponent.onSubmit()
  → this.authService.login(email, senha).subscribe({
      next(usuario) {
        localStorage.setItem('usuarioLogado', JSON.stringify(usuario))
        setTimeout(() => { window.location.href = '/' }, 100)
      },
      error(err) {
        // Erro de login não tratado — implementação básica
      }
    })
```

O `setTimeout` é necessário porque a chamada a `window.location.href` dentro de um `subscribe` pode disparar mudanças após a detecção de mudanças do Angular, causando o erro `ExpressionChangedAfterItHasBeenCheckedError`.

---

## 9. Ciclo de Vida de Componentes

```
Componente Standalone
  constructor()       → Injeta serviços (não faz chamadas HTTP)
  ngOnInit()          → Carrega dados (chamadas HTTP)
  ngOnDestroy()       → Limpa subscriptions se houver (geralmente subscriptions HTTP se completam sozinhas)

AppComponent (topbar):
  constructor(private authService: AuthService)
    → Lê usuário do localStorage
    → Expõe nome, role para o template
    → logout(): remove localStorage + window.location.href = '/login'

LoginComponent:
  → Se já logado (ngOnInit): redireciona para '/'
```

---

## 10. Tratamento de Erros (Backend)

```
Controller
  → Service
    → Se sem permissão: RuntimeException("Acesso negado: ...")
    → Se não encontrado: RuntimeException("Tarefa não encontrada: {id}")
    → Se email duplicado: DataIntegrityViolation → capturado no service → RuntimeException("Email ja cadastrado.")

GlobalExceptionHandler
  @ExceptionHandler(RuntimeException.class)
    → mensagem "Acesso negado:" | "necessaria" → 403
    → mensagem "nao encontrada" | "ja cadastrado" → 400
    → default → 400

  @ExceptionHandler(MethodArgumentNotValidException.class)
    → Concatena field + defaultMessage → 400
```

---

## 11. Inicialização do Banco (DataInitializer)

```
ApplicationRunner.run()
  → Se COUNT(Usuario) == 0:
    → Cria 3 usuários (ADMIN, DISTRIBUIDOR, EXECUTOR)
    → Cria 6 categorias (TRABALHO, ESTUDOS, PESSOAL, URGENTE, SAUDE, FINANCEIRO)
    → Cria 12 tarefas com status variados (PENDENTE, EM_EXECUCAO, CONCLUIDA)
    → Categorias e responsáveis distribuídos
```

---

## 12. Erros Comuns e Debug

| Sintoma | Causa | Solução |
|---------|-------|---------|
| 403 ao criar usuário | Sessão expirou ou usuário não é ADMIN | Relogar como admin |
| "Email ja cadastrado" | Email duplicado no BD | Usar email diferente |
| 400 "nao encontrada" | ID inválido ou deletado | Verificar ID no console |
| Login não redireciona | `router.navigate` em vez de `window.location.href` | Usar `window.location.href` dentro de `setTimeout` |
| Build warning CSS | task-list.css > 4 kB | Reduzir CSS ou aumentar budget no angular.json |
