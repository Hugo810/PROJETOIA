# TaskFlow - Plano de Implementação Completo
## Revisão de Harness + 10 Módulos

---

## 1. ESTADO ATUAL DO PROJETO

### O que já existe funcional:
| Módulo | Status | Cobertura |
|--------|--------|-----------|
| CRUD Tarefas | ✅ Funcional | 80% |
| CRUD Categorias | ✅ Funcional | 90% |
| CRUD Usuários | ✅ Funcional | 70% |
| Login/Logout | ✅ Funcional | 40% |
| Filtros de tarefas | ✅ Funcional | 60% |
| Paginação | ✅ Funcional | 80% |
| Ações em massa | ✅ Funcional | 50% |
| Distribuição de tarefas | ✅ Funcional | 60% |
| Cache (frontend) | ✅ Funcional | 70% |
| Testes backend | ✅ 44 testes | 50% |
| Testes E2E | ✅ 5 testes | 20% |

### Críticos a corrigir antes de evoluir:
1. **Auth via header `X-User-Id`** → Qualquer um se passa por outro
2. **Senhas em plain text** → Sem BCrypt
3. **`RuntimeException` genérica** → Sem exceções de domínio
4. **`window.location.href`** → Quebra SPA
5. **Sem AuthGuard** → Rotas desprotegidas
6. **`CategoriaEnum.java` morto** → Código não utilizado

---

## 2. PRÉ-REQUISITOS (FASE 0)

### 2.1 Corrigir Problemas Críticos Existentes

#### Backend:
- [ ] Criar exceções de domínio: `TarefaNotFoundException`, `CategoriaNotFoundException`, `UsuarioNotFoundException`, `AcessoNegadoException`, `StatusInvalidoException`
- [ ] Atualizar `GlobalExceptionHandler` para tratar cada exceção com HTTP status correto (404, 403, 400, 500)
- [ ] Remover `CategoriaEnum.java` (código morto)
- [ ] Adicionar `@JsonIgnore` no campo `senha` da entidade `Usuario`
- [ ] Adicionar validação de transição de status em `TarefaService`

#### Frontend:
- [ ] Substituir `window.location.href` por `Router.navigate()` em `AppComponent`
- [ ] Criar `AuthGuard` funcional
- [ ] Aplicar `AuthGuard` nas rotas protegidas
- [ ] Atualizar `ConfirmModalComponent` para aceitar `@Input` de mensagem e texto dos botões

---

## 3. MÓDULO 1: GESTÃO DE TAREFAS (Refinamento)

### Backend - O que falta:
```java
// 1. Specification para filtros dinâmicos
public class TarefaSpecification {
    public static Specification<Tarefa> withFilters(
        String status, Long categoriaId, Long responsavelId, String busca
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (categoriaId != null) predicates.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            if (responsavelId != null) predicates.add(cb.equal(root.get("responsavel").get("id"), responsavelId));
            if (busca != null) predicates.add(cb.or(
                cb.like(cb.lower(root.get("titulo")), "%"+busca.toLowerCase()+"%"),
                cb.like(cb.lower(root.get("descricao")), "%"+busca.toLowerCase()+"%")
            ));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

// 2. Validação de transição de status
public enum TransicaoStatus {
    PENDENTE -> EM_EXECUCAO,
    EM_EXECUCAO -> CONCLUIDA,
    EM_EXECUCAO -> PENDENTE  // voltar
}

// 3. Soft delete (opcional)
// Adicionar campo "ativo" boolean em Tarefa
```

### Frontend - O que falta:
```typescript
// 1. Service de busca avançada
// Adicionar parâmetros de busca textual ao TaskService
// 2. Filtros combináveis no servidor
// Atualizar task-list para enviar filtros ao backend em vez de filtrar client-side
// 3. Badge de contagem por status
// Mostrar contagem: "Pendentes (5) | Em Execução (3) | Concluídas (12)"
```

### Endpoint atualizado:
```
GET /api/tarefas?status=&categoriaId=&responsavelId=&busca=&pagina=&tamanho=
```

---

## 4. MÓDULO 2: TAREFAS E AFAZERES

### Backend:
```java
// Novos endpoints necessários
GET /api/tarefas/hoje          // Tarefas com prazo = hoje
GET /api/tarefas/semana        // Tarefas com prazo nesta semana
GET /api/tarefas/atrasadas     // Tarefas com prazo < hoje e status != CONCLUIDA
GET /api/tarefas/proximas      // Próximas 7 dias

// No TarefaRepository:
@Query("SELECT t FROM Tarefa t WHERE t.prazo = CURRENT_DATE AND t.status != 'CONCLUIDA'")
List<Tarefa> findTarefasHoje();

@Query("SELECT t FROM Tarefa t WHERE t.prazo BETWEEN CURRENT_DATE AND CURRENT_DATE + 7 AND t.status != 'CONCLUIDA'")
List<Tarefa> findTarefasSemana();

@Query("SELECT t FROM Tarefa t WHERE t.prazo < CURRENT_DATE AND t.status != 'CONCLUIDA'")
List<Tarefa> findTarefasAtrasadas();
```

### Frontend - Novo componente:
```typescript
// components/daily-tasks/daily-tasks.ts
// - Visualização calendário (mini)
// - Lista de tarefas de hoje com checkbox
// - Contadores: hoje (3), esta semana (8), atrasadas (2)
// - Conclusão rápida via checkbox (PATCH /api/tarefas/{id}/concluir)
```

### Modelo de dados adicionar:
```typescript
interface ResumoTarefas {
  hoje: Tarefa[];
  semana: Tarefa[];
  atrasadas: Tarefa[];
  contagem: {
    hoje: number;
    semana: number;
    atrasadas: number;
  };
}
```

---

## 5. MÓDULO 3: FERRAMENTAS DE COLABORAÇÃO

### Backend - Novas entidades:
```java
@Entity
@Table(name = "comentario")
public class Comentario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 2000)
    private String texto;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private Tarefa tarefa;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;
    
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}

@Entity
@Table(name = "notificacao")
public class Notificacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String mensagem;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id")
    private Tarefa tarefa;
    
    private Boolean lida;
    private LocalDateTime dataCriacao;
    private String tipo; // ATRIBUICAO, COMENTARIO, PRAZO, CONCLUSAO
}

@Entity
@Table(name = "historico_alteracao")
public class HistoricoAlteracao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private Tarefa tarefa;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    private String campo;
    private String valorAnterior;
    private String valorNovo;
    private LocalDateTime dataAlteracao;
}
```

### Novos endpoints:
```
# Comentários
GET    /api/tarefas/{id}/comentarios
POST   /api/tarefas/{id}/comentarios
PUT    /api/comentarios/{id}
DELETE /api/comentarios/{id}

# Notificações
GET    /api/notificacoes?usuarioId=
PATCH  /api/notificacoes/{id}/lida
GET    /api/notificacoes/nao-lidas?usuarioId=

# Histórico
GET    /api/tarefas/{id}/historico
```

### Frontend - Componentes:
```typescript
// components/task-comments/task-comments.ts
// - Lista de comentários da tarefa
// - Formulário para novo comentário
// - Menções com @usuario
// - Timestamp relativo ("há 2 horas")

// components/notifications/notifications.ts
// - sino de notificações na topbar
// - badge com contagem de não lidas
// - dropdown com lista de notificações
// - marcar como lida ao clicar

// components/task-history/task-history.ts
// - Timeline de alterações da tarefa
// - Quem alterou, quando, o quê
```

---

## 6. MÓDULO 4: CONTROLE DE TEMPO

### Backend - Novas entidades:
```java
@Entity
@Table(name = "registro_tempo")
public class RegistroTempo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private Tarefa tarefa;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private Long duracaoMinutos; // calculado ou inserido manualmente
    private String descricao;
    private Boolean手动; // true = registro manual, false = timer
}
```

### Novos endpoints:
```
POST   /api/tarefas/{id}/tempo              // Iniciar timer
PATCH  /api/registros-tempo/{id}/parar       // Parar timer
POST   /api/tarefas/{id}/tempo/manual        // Registro manual
GET    /api/tarefas/{id}/tempo               // Total de tempo na tarefa
GET    /api/usuarios/{id}/tempo?inicio=&fim= // Relatório por usuário
GET    /api/relatorios/tempo?periodo=semana  // Dashboard de tempo
```

### Frontend - Componentes:
```typescript
// components/task-timer/task-timer.ts
// - Botão play/pause para iniciar/parar timer
// - Cronômetro em tempo real (00:12:34)
// - Histórico de sessões na tarefa

// components/time-report/time-report.ts
// - Gráfico de barras: horas por dia
// - Tabela: tempo por tarefa
// - Filtro por período (semana/mês/personalizado)
// - Exportar CSV
```

---

## 7. MÓDULO 5: AUTOMAÇÃO DE TAREFAS

### Backend - Novas entidades:
```java
@Entity
@Table(name = "regra_automacao")
public class RegraAutomacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nome;
    
    @Column(nullable = false)
    private String condicao; // JSON: {"campo":"status","operador":"igual","valor":"CONCLUIDA"}
    
    @Column(nullable = false)
    private String acao; // JSON: {"tipo":"criar_tarefa","dados":{...}}
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_criador_id")
    private Usuario criador;
    
    private Boolean ativa;
    private LocalDateTime dataCriacao;
}
```

### Novos endpoints:
```
GET    /api/automacoes
POST   /api/automacoes
PUT    /api/automacoes/{id}
DELETE /api/automacoes/{id}
PATCH  /api/automacoes/{id}/ativa
```

### Frontend - Componente:
```typescript
// components/automation-rules/automation-rules.ts
// - Lista de regras de automação
// - Formulário visual: SE [condição] ENTÃO [ação]
// - Condições disponíveis: status mudou, prazo chegou, tarefa criada
// - Ações disponíveis: criar tarefa, enviar notificação, atribuir responsável
```

---

## 8. MÓDULO 6: RASTREADOR DE TAREFAS (Dashboard)

### Backend - Novos endpoints:
```
GET /api/dashboard/resumo         // Resumo geral
GET /api/dashboard/por-status     // Contagem por status
GET /api/dashboard/por-categoria  // Contagem por categoria
GET /api/dashboard/por-responsavel // Tarefas por responsável
GET /api/dashboard/tendencia      // Tendência últimos 30 dias
GET /api/dashboard/atrasadas      // Tarefas atrasadas
```

### DTOs:
```java
public record DashboardResumo(
    long totalTarefas,
    long pendentes,
    long emExecucao,
    long concluidas,
    long atrasadas,
    long criadasUltimos7Dias,
    long concluidasUltimos7Dias
) {}

public record TarefasPorStatus(String status, long contagem) {}
public record TarefasPorCategoria(String categoria, long contagem) {}
public record TarefasPorResponsavel(String responsavel, long total, long concluidas) {}
public record Tendencia(String data, long criadas, long concluidas) {}
```

### Frontend - Componente principal:
```typescript
// components/dashboard/dashboard.ts
// - Cards de resumo: total, pendentes, em execução, concluídas, atrasadas
// - Gráfico de pizza: por status
// - Gráfico de barras: por categoria
// - Gráfico de linha: tendência últimos 30 dias
// - Tabela: tarefas atrasadas (acao rápida)
// - Top responsáveis com taxa de conclusão
```

---

## 9. MÓDULO 7: GANTT E CRONOGRAMA

### Backend - Novas entidades:
```java
@Entity
@Table(name = "dependencia_tarefa")
public class DependenciaTarefa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private Tarefa tarefa;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_dependente_id", nullable = false)
    private Tarefa tarefaDependente;
    
    private String tipo; // BLOQUEIA, BLOQUEADA_POR
}

@Entity
@Table(name = "marco")
public class Marco {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nome;
    
    private LocalDateTime data;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id")
    private Projeto projeto; // se houver entidade Projeto
}
```

### Novos endpoints:
```
GET    /api/tarefas/{id}/dependencias
POST   /api/tarefas/{id}/dependencias
DELETE /api/dependencias/{id}
GET    /api/gantt?projetoId=&inicio=&fim=  // Dados para Gantt
```

### Frontend - Componente:
```typescript
// components/gantt-chart/gantt-chart.ts
// - Diagrama de Gantt interativo com canvas/SVG
// - Barras de tarefas com cores por status
// - Linhas de dependência entre tarefas
// - Zoom in/out (dia/semana/mês)
// - Arrastar para ajustar datas
// - Marcos (diamond markers)
// - Hover com detalhes da tarefa
```

---

## 10. MÓDULO 8: PLANEJAMENTO DE TAREFAS

### Backend - Novas entidades:
```java
@Entity
@Table(name = "projeto")
public class Projeto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nome;
    
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String status; // PLANEJADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;
    
    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL)
    private List<Tarefa> tarefas;
}

@Entity
@Table(name = "meta")
public class Meta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titulo;
    
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String status;
    private Double progresso; // 0.0 a 1.0
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id")
    private Projeto projeto;
}
```

### Novos endpoints:
```
# Projetos
GET    /api/projetos
POST   /api/projetos
GET    /api/projetos/{id}
PUT    /api/projetos/{id}
DELETE /api/projetos/{id}

# Metas
GET    /api/projetos/{id}/metas
POST   /api/projetos/{id}/metas
PUT    /api/metas/{id}
```

### Frontend - Componentes:
```typescript
// components/project-list/project-list.ts
// - Lista de projetos com progresso
// - Criar/editar projeto

// components/project-board/project-board.ts
// - Kanban board: Planejado → Em Andamento → Concluído
// - Drag and drop entre colunas
// - Cards de tarefas do projeto
```

---

## 11. MÓDULO 9: TAREFAS RECORRENTES

### Backend - Novas entidades:
```java
@Entity
@Table(name = "tarefa_recorrente")
public class TarefaRecorrente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_modelo_id", nullable = false)
    private Tarefa tarefaModelo; // template da tarefa
    
    @Column(nullable = false)
    private String recorrencia; // DIARIA, SEMANAL, MENSAL, PERSONALIZADA
    
    private String configuracao; // JSON: {"diasSemana":[1,3,5], "diaMes":15}
    
    private LocalDate proximaExecucao;
    private Boolean ativa;
    
    @OneToMany(mappedBy = "tarefaRecorrente")
    private List<Tarefa> ocorrencias;
}
```

### Novos endpoints:
```
GET    /api/tarefas-recorrentes
POST   /api/tarefas-recorrentes
PUT    /api/tarefas-recorrentes/{id}
DELETE /api/tarefas-recorrentes/{id}
PATCH  /api/tarefas-recorrentes/{id}/pular       // Pular próxima ocorrência
PATCH  /api/tarefas-recorrentes/{id}/adiar        // Adiar N dias
GET    /api/tarefas-recorrentes/{id}/historico    // Histórico de execuções
```

### Frontend - Componente:
```typescript
// components/recurring-tasks/recurring-tasks.ts
// - Configuração visual de recorrência
// - Calendário mostrando próximas ocorrências
// - Histórico de execuções passadas
// - Opção de pular/adiar
```

---

## 12. MÓDULO 10: OUTROS

### 12.1 API REST documentada (Swagger):
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```
```
// Acessível em: http://localhost:8080/swagger-ui.html
```

### 12.2 Exportação:
```
GET /api/exportar/tarefas?formato=csv
GET /api/exportar/tarefas?formato=json
GET /api/relatorios/tempo?formato=csv
```

### 12.3 Importação:
```
POST /api/importar/tarefas
// Aceita CSV ou JSON com array de tarefas
```

### 12.4 Integração calendário:
```
GET /api/tarefas/ics?usuarioId=  // Formato iCalendar
GET /api/tarefas/google-calendar?usuarioId=  // Redirect para Google Calendar
```

---

## 13. INFRAESTRUTURA NECESSÁRIA

### Backend - Dependências adicionar ao pom.xml:
```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>

<!-- Flyway -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>

<!-- Springdoc OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>

<!-- Actuator -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Frontend - Dependências adicionar ao package.json:
```json
{
  "dependencies": {
    "@angular/cdk": "^21.0.0",
    "chart.js": "^4.4.0",
    "ng2-charts": "^6.0.0",
    "dayjs": "^1.11.0"
  }
}
```

---

## 14. ORDEM DE IMPLEMENTAÇÃO RECOMENDADA

### Fase 1 - Fundação (Semana 1-2)
1. ✅ Corrigir problemas críticos (exceções, auth header, plain text)
2. Implementar Spring Security + BCrypt
3. Implementar JWT (login gera token, interceptor envia token)
4. Criar AuthGuard no frontend
5. Flyway para migrações

### Fase 2 - Módulos Core (Semana 3-4)
6. Módulo 1: Refinar gestão de tarefas (Specification, busca textual)
7. Módulo 2: Tarefas e afazeres (hoje, semana, atrasadas)
8. Módulo 6: Dashboard de métricas básico

### Fase 3 - Colaboração (Semana 5-6)
9. Módulo 3: Comentários + Notificações + Histórico
10. Módulo 4: Controle de tempo (timer + registros)

### Fase 4 - Avançado (Semana 7-8)
11. Módulo 9: Tarefas recorrentes
12. Módulo 5: Automação básica
13. Módulo 7: Gantt (visualização)

### Fase 5 - Premium (Semana 9-10)
14. Módulo 8: Projetos e planejamento
15. Módulo 7: Gantt interativo (drag & drop)
16. Módulo 5: Automação avançada

### Fase 6 - Polish (Semana 11-12)
17. Módulo 10: Swagger, exportação, importação
18. Testes E2E completos
19. Otimização de performance
20. Documentação final

---

## 15. PADRÕES A SEGUIR

### Backend:
- Exceções específicas herdam de `RuntimeException`
- Services usam `@Service` + `@Slf4j` (Lombok)
- Controllers retornam `ResponseEntity`
- DTOs usam records Java
- Validações via `@Valid` + Bean Validation
- Paginação via `Pageable` do Spring Data

### Frontend:
- Componentes standalone (sem NgModule)
- Reactive Forms (não Template-driven)
- Services singleton com `providedIn: 'root'`
- Lazy loading nas rotas
- `Router.navigate()` nunca `window.location.href`
- Modal reutilizável com `@Input/@Output`

### Testes:
- Backend: MockMvc para controllers, Mockito para services, DataJpaTest para repos
- Frontend: Vitest para unit, Playwright para E2E
- Cobertura mínima: 70% services, 50% components

---

## 16. MODELO DE DADOS FINAL (ER)

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│   USUARIO    │     │     TAREFA       │     │  CATEGORIA   │
├──────────────┤     ├──────────────────┤     ├──────────────┤
│ PK id        │◄─┐  │ PK id            │  ┌─►│ PK id        │
│ nome         │  │  │ titulo           │  │  │ nome         │
│ email (UQ)   │  │  │ descricao        │  │  └──────────────┘
│ senha        │  │  │ FK categoria_id  │──┘
│ role         │  │  │ prazo            │
│ ativo        │  │  │ status           │
└──────┬───────┘  │  │ FK responsavel_id│──┐
       │          │  │ distribuidor_id  │  │
       │          │  │ data_criacao     │  │
       │          │  │ data_conclusao   │  │
       │          │  │ FK projeto_id    │──┼──┐
       │          │  │ recorrente_id    │──┼──┼──┐
       │          │  └──────────────────┘  │  │  │
       │          │                        │  │  │
       │          │  ┌──────────────────┐  │  │  │
       │          │  │   COMENTARIO     │  │  │  │
       │          │  ├──────────────────┤  │  │  │
       │          └─►│ FK tarefa_id     │  │  │  │
       │             │ FK autor_id      │──┘  │  │
       │             │ texto            │     │  │
       │             │ data_criacao     │     │  │
       │             └──────────────────┘     │  │
       │                                      │  │
       │          ┌──────────────────┐        │  │
       │          │   NOTIFICACAO    │        │  │
       │          ├──────────────────┤        │  │
       └─────────►│ FK usuario_id    │        │  │
                  │ FK tarefa_id     │        │  │
                  │ mensagem         │        │  │
                  │ lida             │        │  │
                  │ tipo             │        │  │
                  └──────────────────┘        │  │
                                              │  │
       ┌──────────────────┐                   │  │
       │ HISTORICO_ALTER  │                   │  │
       ├──────────────────┤                   │  │
       │ FK tarefa_id     │───────────────────┘  │
       │ FK usuario_id    │                      │
       │ campo            │                      │
       │ valor_anterior   │                      │
       │ valor_novo       │                      │
       └──────────────────┘                      │
                                                  │
       ┌──────────────────┐     ┌──────────────┐  │
       │  REGISTRO_TEMPO  │     │    PROJETO   │  │
       ├──────────────────┤     ├──────────────┤  │
       │ FK tarefa_id     │     │ PK id        │◄─┘
       │ FK usuario_id    │     │ nome         │
       │ inicio           │     │ descricao    │
       │ fim              │     │ data_inicio  │
       │ duracao_minutos  │     │ data_fim     │
       │ manual           │     │ status       │
       └──────────────────┘     │ FK resp_id   │
                                └──────┬───────┘
                                       │
       ┌──────────────────┐            │
       │ DEPENDENCIA      │            │
       ├──────────────────┤            │
       │ FK tarefa_id     │            │
       │ FK tarefa_dep_id │            │
       │ tipo             │            │
       └──────────────────┘            │
                                       │
       ┌──────────────────┐            │
       │  REGRA_AUTOMACAO │            │
       ├──────────────────┤            │
       │ FK criador_id    │────────────┘
       │ nome             │
       │ condicao (JSON)  │
       │ acao (JSON)      │
       │ ativa            │
       └──────────────────┘

       ┌──────────────────┐
       │ TAREFA_RECORRENTE│
       ├──────────────────┤
       │ FK tarefa_modelo │
       │ recorrencia      │
       │ configuracao     │
       │ proxima_execucao │
       │ ativa            │
       └──────────────────┘
```

---

## 17. CHECKLIST FINAL DE VALIDAÇÃO

### Para cada módulo implementado:
- [ ] Entidade JPA criada com todas as anotações
- [ ] Repository com queries otimizadas
- [ ] Service com lógica de negócio e validações
- [ ] Controller REST com endpoints RESTful
- [ ] DTOs para transferência de dados
- [ ] Exceções específicas do domínio
- [ ] Testes unitários do service (Mockito)
- [ ] Testes de integração do controller (MockMvc)
- [ ] Componente Angular correspondente
- [ ] Service Angular para consumir API
- [ ] Rotas configuradas com AuthGuard
- [ ] Testes E2E do fluxo principal
- [ ] Documentação Swagger atualizada
