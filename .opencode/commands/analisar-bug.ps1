# ============================================================
# analisar-bug.ps1 — Análise de Causa Raiz de Bug (Windows)
# ============================================================
# Uso: .\analisar-bug.ps1 "descrição do bug"
# ============================================================

param([string]$BugDesc = "")

function Write-Step($text) { Write-Host ">>> $text" -ForegroundColor Yellow }
function Write-OK($text) { Write-Host "  $text" -ForegroundColor Green }
function Write-Info($text) { Write-Host "  $text" -ForegroundColor Cyan }
function Write-Error($text) { Write-Host "  $text" -ForegroundColor Red }

if (-not $BugDesc) {
  Write-Error "Uso: .\analisar-bug.ps1 `"descrição do bug`""
  exit 1
}

Write-Host "============================================" -ForegroundColor Blue
Write-Host "  ANALISADOR DE CAUSA RAIZ" -ForegroundColor Blue
Write-Host "============================================" -ForegroundColor Blue
Write-Host ""
Write-Host "Bug: " -NoNewline; Write-Host "$BugDesc" -ForegroundColor Yellow
Write-Host ""

# Passo 1
Write-Step "[1/5] Perguntas de clarificação"
$esperado = Read-Host "Q1: Qual o comportamento esperado"
$atual = Read-Host "Q2: Qual o comportamento atual"
$repro = Read-Host "Q3: Como reproduzir"
Write-Host ""

# Passo 2
Write-Step "[2/5] Coleta de evidências"
Write-Info "Buscando arquivos relacionados..."

$palavras = ($BugDesc + " " + $esperado + " " + $atual) -split '\s+' | Sort-Object -Unique
$candidatos = @()

foreach ($palavra in $palavras) {
  if ($palavra.Length -ge 3) {
    $found = Get-ChildItem -Recurse -Include "*.ts", "*.java", "*.html" | 
             Select-String -Pattern $palavra -SimpleMatch -ErrorAction SilentlyContinue |
             Select-Object -ExpandProperty Path -Unique |
             Select-Object -First 5
    foreach ($f in $found) {
      if ($candidatos -notcontains $f) { $candidatos += $f }
    }
  }
}

if ($candidatos.Count -gt 0) {
  Write-OK "Arquivos potencialmente relevantes:"
  foreach ($f in $candidatos) { Write-Info "  - $f" }
} else {
  Write-Info "(Nenhum arquivo encontrado por palavra-chave)"
}

# Últimos commits
if (Test-Path ".git") {
  Write-Host ""
  Write-Info "Últimos 5 commits:"
  git log --oneline -5 2>$null | ForEach-Object { Write-Info "  $_" }
}

Write-Host ""

# Passo 3
Write-Step "[3/5] Análise de logs disponíveis"
try {
  $health = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
  Write-OK "Backend OK (porta 8080)"
} catch {
  Write-Host "  Backend: não respondeu em 8080"
}

try {
  $front = Invoke-WebRequest -Uri "http://localhost:4200" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
  Write-OK "Frontend OK (porta 4200)"
} catch {
  Write-Host "  Frontend: não respondeu em 4200"
}

Write-Host ""

# Passo 4
Write-Step "[4/5] Identificação da causa raiz"
Write-Host "  Bug: $BugDesc"
Write-Host "  Esperado: $esperado"
Write-Host "  Atual: $atual"
Write-Host "  Reprodução: $repro"
Write-Host ""
Write-Host "  Causa raiz hipotética:"
Write-Host "    Gap entre comportamento esperado e atual"
Write-Host "    Contribuintes: código não reflete spec, testes insuficientes, side effect"

if (Test-Path ".git") {
  Write-Host ""
  Write-Info "Arquivos modificados no último commit:"
  git diff --name-only HEAD~1 2>$null | ForEach-Object { Write-Info "  - $_" }
}

Write-Host ""

# Passo 5
Write-Step "[5/5] Sugestão de correção"
Write-Host "  Arquivos afetados:"
foreach ($f in $candidatos) { Write-Info "  - $f" }
Write-Host ""

$autoFix = Read-Host "Deseja iniciar /corrigir-bug automaticamente? (s/N)"
if ($autoFix -eq "s" -or $autoFix -eq "S") {
  Write-OK "Modo correção ativado"
  Write-Info "Bug: $BugDesc"
  Write-Info "Causa: gap entre esperado ($esperado) e atual ($atual)"
  Write-Info "Arquivos: $($candidatos -join ', ')"
}
