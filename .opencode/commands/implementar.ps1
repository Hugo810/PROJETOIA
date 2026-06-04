param([string]$TaskDesc = "")

if (-not $TaskDesc) {
  Write-Host "Uso: .\implementar.ps1 `"descrição da task`"" -ForegroundColor Red
  exit 1
}

Write-Host "============================================" -ForegroundColor Blue
Write-Host "  DECOMPOSIÇÃO DE TASK (TDD)" -ForegroundColor Blue
Write-Host "============================================" -ForegroundColor Blue
Write-Host ""
Write-Host "Task: " -NoNewline; Write-Host $TaskDesc -ForegroundColor Yellow
Write-Host ""

# Fase 1 — Clarificação
Write-Host "[Fase 1] Perguntas de clarificação" -ForegroundColor Yellow
$escopo = Read-Host "P1: Qual o escopo? (backend/frontend/ambos)"
$criterios = Read-Host "P2: Quais os critérios de aceitação"
$ui = Read-Host "P3: Existe interface de usuário? (s/N)"
Write-Host ""

# Fase 2 — Plano
Write-Host "[Fase 2] Plano de implementação" -ForegroundColor Yellow
Write-Host ""

$tasks = @()
$tests = @()

if ($escopo -eq "frontend" -or $escopo -eq "ambos") {
  Write-Host "Sprint FE: Componente + Serviço" -ForegroundColor Magenta
  $tasks += "Task 1 - Criar modelo/interface"
  $tests += "  Teste 1: modelo possui campos corretos"
  $tests += "  Teste 2: instância default tem valores esperados"
  $tasks += "Task 2 - Criar serviço HTTP"
  $tests += "  Teste 1: método listar chama GET /api/..."
  $tests += "  Teste 2: método criar chama POST /api/..."
  $tasks += "Task 3 - Criar componente com template"
  $tests += "  Teste 1: componente renderiza dados mockados"
  $tests += "  Teste 2: evento de clique propaga"
  if ($ui -eq "s" -or $ui -eq "S") {
    $tasks += "Task 4 - Teste E2E do fluxo"
    $tests += "  Teste 1: Playwright — criar via UI"
    $tests += "  Teste 2: Playwright — editar e confirmar"
  }
}

if ($escopo -eq "backend" -or $escopo -eq "ambos") {
  Write-Host "Sprint BE: Controller + Service + Repository" -ForegroundColor Magenta
  $tasks += "Task 1 - Criar entidade JPA"
  $tests += "  Teste 1: @DataJpaTest — save e findById"
  $tests += "  Teste 2: @DataJpaTest — derived query"
  $tasks += "Task 2 - Criar DTO e Service"
  $tests += "  Teste 1: Mockito — criar retorna DTO"
  $tests += "  Teste 2: Mockito — erro 404 se id inválido"
  $tasks += "Task 3 - Criar Controller REST"
  $tests += "  Teste 1: MockMvc — POST retorna 201"
  $tests += "  Teste 2: MockMvc — GET lista retorna 200"
}

Write-Host ""
Write-Host "Tasks:" -ForegroundColor Blue
for ($i = 0; $i -lt $tasks.Count; $i++) {
  Write-Host ""
  Write-Host "  $($tasks[$i])" -ForegroundColor Cyan
  $testIdx = $i * 2
  if ($testIdx -lt $tests.Count) { Write-Host "    $($tests[$testIdx])" }
  if (($testIdx + 1) -lt $tests.Count) { Write-Host "    $($tests[$testIdx + 1])" }
}

Write-Host ""
Write-Host "[Fase 3] Verificação" -ForegroundColor Yellow
if ($escopo -eq "frontend" -or $escopo -eq "ambos") {
  Write-Host "  cd frontend && npm run test"
  if ($ui -eq "s" -or $ui -eq "S") { Write-Host "  cd frontend && npx playwright test" }
}
if ($escopo -eq "backend" -or $escopo -eq "ambos") {
  Write-Host "  cd backend && mvnw test"
}

Write-Host ""
Write-Host "Plano gerado. Comece pela Task 1 (Red → Green → Refactor)." -ForegroundColor Green
