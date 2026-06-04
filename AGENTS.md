# AGENTS.md - TaskFlow

**Para OpenCode** (equivalente ao CLAUDE.md/CLAUDE.local)

## Stack Real (ambiente sem Docker)
- **Frontend**: Angular 21 (standalone, Vitest)
- **Backend**: Spring Boot 3.2.5 + Java 19
- **Database**: H2 (modo Oracle) - migração futura para Oracle real
- **Testes BE**: JUnit 5 + MockMvc (sem Testcontainers)
- **Testes FE**: Vitest + Playwright E2E

## Estrutura de pastas
```
backend/
├── src/main/java/com/taskflow/
│   ├── config/        (DataInitializer, GlobalExceptionHandler)
│   ├── controller/    (HealthController, CategoriaController, TarefaController)
│   ├── dto/           (CategoriaDTO, TarefaDTO)
│   ├── model/         (Categoria, CategoriaEnum, Tarefa)
│   ├── repository/    (CategoriaRepository, TarefaRepository)
│   └── service/       (CategoriaService, TarefaService)
├── src/test/java/com/taskflow/
│   ├── controller/    (CategoriaControllerTest, TarefaControllerTest) - MockMvc
│   ├── repository/    (TarefaRepositoryTest) - @DataJpaTest
│   └── service/       (CategoriaServiceTest, TarefaServiceTest) - Mockito
└── pom.xml            (Spring Boot 3.2.5, Java 19, H2 + Oracle driver)

frontend/
├── e2e/               (Playwright - 5 testes E2E)
└── src/app/
    ├── components/
    │   ├── confirm-modal/  (modal de exclusão)
    │   ├── task-form/      (reactive forms)
    │   └── task-list/      (listagem + filtros + paginação)
    ├── models/             (task.ts, category.ts)
    └── services/           (task.service.ts)
```

## Regra de Setup (OBRIGATÓRIO)

**NUNCA** rode `npm install` diretamente sem antes:
1. Verificar se `frontend/node_modules` existe
2. Usar `npm ci --prefer-offline` em vez de `npm install`
3. Se for primeira vez, execute `scripts/setup.sh` UMA vez no terminal real

**Proibido** para o agente:
- Rodar `npm install` em projetos Angular (muito lento)
- Indexar arquivos dentro de `node_modules/`

## Como rodar (sem Docker)

### Backend
```bash
cd backend
mvnw spring-boot:run
# Acessar: http://localhost:8080/api/health
# H2 console: http://localhost:8080/h2-console (JDBC: jdbc:h2:mem:taskflow;MODE=Oracle)
```

### Frontend
```bash
cd frontend
ng serve --open                    # Dev com proxy (/api -> 8080)
# Acessar: http://localhost:4200
```

## Testes

### Backend (44 testes)
```bash
cd backend
mvnw test                          # Compilar + testar
```

### Frontend
```bash
cd frontend
npm run e2e                        # Testes E2E Playwright (servidor precisa estar rodando)
```

## Acessos
| URL | Descrição |
|-----|-----------|
| http://localhost:4200 | Frontend |
| http://localhost:8080/api | API REST |
| http://localhost:8080/api/health | Health Check |
| http://localhost:8080/h2-console | H2 Console (jdbc:h2:mem:taskflow) |

## Comandos úteis para debug

### H2 Console
```
URL:   http://localhost:8080/h2-console
JDBC:  jdbc:h2:mem:taskflow;MODE=Oracle;DB_CLOSE_DELAY=-1
User:  sa
Pass:  (vazio)
```

### Reset do banco de dados
```bash
cd backend
rm -rf target
./mvnw clean
```

### Porta 8080 em uso
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Limpar cache do Angular
```bash
rm -rf frontend/.angular/cache
```

### Verificar logs do backend (SLF4J)
```bash
cd backend
./mvnw spring-boot:run 2>&1 | grep "com.taskflow"
```

## APIs
- `GET    /api/health` - Health check
- `GET    /api/tarefas?status=&categoriaId=&pagina=&tamanho=` - Listar tarefas
- `GET    /api/tarefas/{id}` - Buscar tarefa
- `POST   /api/tarefas` - Criar tarefa
- `PUT    /api/tarefas/{id}` - Atualizar tarefa
- `PATCH  /api/tarefas/{id}/concluir` - Concluir tarefa
- `DELETE /api/tarefas/{id}` - Excluir tarefa
- `GET    /api/categorias` - Listar categorias
